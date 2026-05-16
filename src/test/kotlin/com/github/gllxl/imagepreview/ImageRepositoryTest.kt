package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.model.ImageLoadState
import com.github.gllxl.imagepreview.model.ImageResource
import com.github.gllxl.imagepreview.service.ImageLoader
import com.github.gllxl.imagepreview.service.ImageLoadRequestKind
import com.github.gllxl.imagepreview.service.ImageRepository
import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import java.awt.image.BufferedImage
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class ImageRepositoryTest {

  private val imageUrl = "https://cdn.example.com/image.png"
  private lateinit var executor: ExecutorService

  @BeforeTest
  fun setUp() {
    executor = Executors.newFixedThreadPool(ImageRepository.MAX_CONCURRENT_REQUESTS)
  }

  @AfterTest
  fun tearDown() {
    executor.shutdownNow()
  }

  @Test
  fun testRequestImageLoadsAndCachesImage() {
    val calls = AtomicInteger()
    val loadedImage = AtomicReference<ImageResource>()
    val loaded = CountDownLatch(1)
    val repository = repository {
      calls.incrementAndGet()
      imageResource(32, 16, "12.5 kB")
    }

    repository.requestImage(imageUrl) {
      loadedImage.set(it)
      loaded.countDown()
    }

    assertTrue(loaded.await(3, TimeUnit.SECONDS), "image load callback should run")
    assertEquals(1, calls.get())
    assertEquals(32, loadedImage.get().imageBuffered.width)
    assertEquals(16, loadedImage.get().imageBuffered.height)
    assertEquals("12.5 kB", loadedImage.get().imageSize)
    assertEquals(loadedImage.get(), repository.getImage(imageUrl))
    assertIs<ImageLoadState.Ready>(repository.getState(imageUrl))
  }

  @Test
  fun testConcurrentRequestsShareOneFetchAndFanOutCallbacks() {
    val calls = AtomicInteger()
    val fetchStarted = CountDownLatch(1)
    val releaseFetch = CountDownLatch(1)
    val callbacks = CountDownLatch(2)
    val repository = repository {
      calls.incrementAndGet()
      fetchStarted.countDown()
      assertTrue(releaseFetch.await(3, TimeUnit.SECONDS), "test fetch should be released")
      imageResource(48, 24, "24 kB")
    }

    repository.requestImage(imageUrl) {
      callbacks.countDown()
    }

    assertTrue(fetchStarted.await(3, TimeUnit.SECONDS), "first request should start fetching")
    assertIs<ImageLoadState.Loading>(repository.getState(imageUrl))

    repository.requestImage(imageUrl) {
      callbacks.countDown()
    }

    releaseFetch.countDown()

    assertTrue(callbacks.await(3, TimeUnit.SECONDS), "both callbacks should receive the loaded image")
    assertEquals(1, calls.get())
    assertNotNull(repository.getImage(imageUrl))
  }

  @Test
  fun testCachedRequestDoesNotFetchAgain() {
    val calls = AtomicInteger()
    val firstLoad = CountDownLatch(1)
    val cachedLoad = CountDownLatch(1)
    val repository = repository {
      calls.incrementAndGet()
      imageResource(10, 10, "100 B")
    }

    repository.requestImage(imageUrl) {
      firstLoad.countDown()
    }
    assertTrue(firstLoad.await(3, TimeUnit.SECONDS), "first callback should run")

    repository.requestImage(imageUrl) {
      cachedLoad.countDown()
    }
    assertTrue(cachedLoad.await(3, TimeUnit.SECONDS), "cached callback should run")

    assertEquals(1, calls.get())
  }

  @Test
  fun testFailedFetchDoesNotPopulateCacheAndRetriesAfterBackoff() {
    val calls = AtomicInteger()
    val now = AtomicLong(1_000)
    val firstFetch = CountDownLatch(1)
    val secondLoad = CountDownLatch(1)
    val repository = repository(
      nowProvider = { now.get() },
      failureBackoffMs = 10_000,
    ) {
      val attempt = calls.incrementAndGet()
      if (attempt == 1) {
        firstFetch.countDown()
        null
      } else {
        imageResource(20, 20, "200 B")
      }
    }

    repository.requestImage(imageUrl) {
      fail("failed image loads should not notify callbacks")
    }

    assertTrue(firstFetch.await(3, TimeUnit.SECONDS), "first fetch should complete")
    assertNull(repository.getImage(imageUrl))
    eventually("failed URL should enter failure state") {
      repository.getState(imageUrl) is ImageLoadState.Failed
    }

    eventually("failed URL should not be fetched again inside the backoff window") {
      repository.requestImage(imageUrl) {
        fail("backoff should suppress callbacks")
      }
      calls.get() == 1
    }

    now.addAndGet(10_001)
    repository.requestImage(imageUrl) {
      secondLoad.countDown()
    }

    assertTrue(secondLoad.await(3, TimeUnit.SECONDS), "retry should load successfully")
    assertEquals(2, calls.get())
    assertNotNull(repository.getImage(imageUrl))
  }

  @Test
  fun testLoaderExceptionsAreContainedAndRememberedAsFailures() {
    val calls = AtomicInteger()
    val failed = CountDownLatch(1)
    val repository = repository {
      calls.incrementAndGet()
      failed.countDown()
      error("broken response")
    }

    repository.requestImage(imageUrl) {
      fail("exceptions should not notify success callbacks")
    }

    assertTrue(failed.await(3, TimeUnit.SECONDS), "failing loader should run")
    eventually("failed URL should enter failure state") {
      repository.getState(imageUrl) is ImageLoadState.Failed
    }
    assertEquals(1, calls.get())
    assertNull(repository.getImage(imageUrl))
  }

  @Test
  fun testImageLoadsUseBoundedConcurrency() {
    val calls = AtomicInteger()
    val running = AtomicInteger()
    val maxRunning = AtomicInteger()
    val firstWaveStarted = CountDownLatch(ImageRepository.MAX_CONCURRENT_REQUESTS)
    val releaseFetches = CountDownLatch(1)
    val completed = CountDownLatch(6)
    val repository = repository {
      calls.incrementAndGet()
      val active = running.incrementAndGet()
      maxRunning.updateAndGet { previous -> maxOf(previous, active) }
      firstWaveStarted.countDown()
      try {
        assertTrue(releaseFetches.await(3, TimeUnit.SECONDS), "test fetches should be released")
        imageResource(2, 2, "2 B")
      } finally {
        running.decrementAndGet()
      }
    }

    repeat(6) {
      repository.requestImage("https://cdn.example.com/image-$it.png") {
        completed.countDown()
      }
    }

    assertTrue(firstWaveStarted.await(3, TimeUnit.SECONDS), "first bounded wave should start")
    assertEquals(
      ImageRepository.MAX_CONCURRENT_REQUESTS,
      calls.get(),
      "only the bounded wave should run before blocked fetches are released",
    )

    releaseFetches.countDown()

    assertTrue(completed.await(3, TimeUnit.SECONDS), "all queued requests should complete")
    assertEquals(6, calls.get())
    assertTrue(
      maxRunning.get() <= ImageRepository.MAX_CONCURRENT_REQUESTS,
      "image fetch concurrency should stay bounded",
    )
  }

  @Test
  fun testInvalidUrlIsIgnoredBeforeFetching() {
    val calls = AtomicInteger()
    val repository = repository {
      calls.incrementAndGet()
      imageResource(1, 1, "1 B")
    }

    repository.requestImage("//cdn.example.com/image.png")

    assertEquals(0, calls.get())
    assertNull(repository.getImage("//cdn.example.com/image.png"))
    assertIs<ImageLoadState.NotRequested>(repository.getState("//cdn.example.com/image.png"))
  }

  @Test
  fun testRemoteAutomaticLoadingCanBeDisabledButExplicitPreviewStillLoads() {
    val calls = AtomicInteger()
    val explicitLoad = CountDownLatch(1)
    val repository = repository(
      settings = ImagePreviewSettings.StateData(isLoadRemoteImagesAutomatically = false),
    ) {
      calls.incrementAndGet()
      imageResource(12, 8, "8 B")
    }

    repository.requestImage(imageUrl) {
      fail("automatic remote loading should be disabled")
    }

    assertEquals(0, calls.get())
    assertIs<ImageLoadState.NotRequested>(repository.getState(imageUrl))

    repository.requestImage(imageUrl, requestKind = ImageLoadRequestKind.EXPLICIT) {
      explicitLoad.countDown()
    }

    assertTrue(explicitLoad.await(3, TimeUnit.SECONDS), "explicit preview request should still load")
    assertEquals(1, calls.get())
  }

  @Test
  fun testLocalAutomaticLoadingCanBeDisabledButExplicitPreviewStillLoads() {
    val localImageUrl = "file:///tmp/image-preview-local.png"
    val calls = AtomicInteger()
    val explicitLoad = CountDownLatch(1)
    val repository = repository(
      settings = ImagePreviewSettings.StateData(isLoadLocalImagesAutomatically = false),
    ) {
      calls.incrementAndGet()
      imageResource(14, 9, "9 B")
    }

    repository.requestImage(localImageUrl) {
      fail("automatic local loading should be disabled")
    }

    assertEquals(0, calls.get())
    assertIs<ImageLoadState.NotRequested>(repository.getState(localImageUrl))

    repository.requestImage(localImageUrl, requestKind = ImageLoadRequestKind.EXPLICIT) {
      explicitLoad.countDown()
    }

    assertTrue(explicitLoad.await(3, TimeUnit.SECONDS), "explicit local preview request should still load")
    assertEquals(1, calls.get())
  }

  @Test
  fun testLoadOnlyWhenPreviewIsClickedSuppressesAutomaticRequests() {
    val calls = AtomicInteger()
    val explicitLoad = CountDownLatch(1)
    val repository = repository(
      settings = ImagePreviewSettings.StateData(isLoadOnlyWhenPreviewIsClicked = true),
    ) {
      calls.incrementAndGet()
      imageResource(18, 9, "10 B")
    }

    repository.requestImage(imageUrl) {
      fail("load-only-on-click should suppress automatic loading")
    }

    assertEquals(0, calls.get())

    repository.requestImage(imageUrl, requestKind = ImageLoadRequestKind.EXPLICIT) {
      explicitLoad.countDown()
    }

    assertTrue(explicitLoad.await(3, TimeUnit.SECONDS), "explicit preview request should load")
    assertEquals(1, calls.get())
  }

  @Test
  fun testAllowedDomainsBlocksRemoteImagesForAutomaticAndExplicitRequests() {
    val calls = AtomicInteger()
    val repository = repository(
      settings = ImagePreviewSettings.StateData(allowedDomains = "images.example.com"),
    ) {
      calls.incrementAndGet()
      imageResource(1, 1, "1 B")
    }

    repository.requestImage(imageUrl) {
      fail("blocked automatic request should not load")
    }
    repository.requestImage(imageUrl, requestKind = ImageLoadRequestKind.EXPLICIT) {
      fail("blocked explicit request should not load")
    }

    assertEquals(0, calls.get())
    assertIs<ImageLoadState.NotRequested>(repository.getState(imageUrl))
  }

  @Test
  fun testAllowedDomainsSupportsWildcardSubdomains() {
    val calls = AtomicInteger()
    val loaded = CountDownLatch(1)
    val repository = repository(
      settings = ImagePreviewSettings.StateData(allowedDomains = "*.example.com"),
    ) {
      calls.incrementAndGet()
      imageResource(20, 10, "2 B")
    }

    repository.requestImage("https://deep.cdn.example.com/image.png") {
      loaded.countDown()
    }

    assertTrue(loaded.await(3, TimeUnit.SECONDS), "wildcard domain should allow subdomains")
    assertEquals(1, calls.get())
  }

  @Test
  fun testClearPreventsInFlightRequestFromRepopulatingCache() {
    val fetchStarted = CountDownLatch(1)
    val releaseFetch = CountDownLatch(1)
    val fetchFinished = CountDownLatch(1)
    val repository = repository {
      fetchStarted.countDown()
      assertTrue(releaseFetch.await(3, TimeUnit.SECONDS), "test fetch should be released")
      fetchFinished.countDown()
      imageResource(40, 20, "4 B")
    }

    repository.requestImage(imageUrl) {
      fail("callbacks registered before clear should be discarded")
    }

    assertTrue(fetchStarted.await(3, TimeUnit.SECONDS), "fetch should start before cache is cleared")
    repository.clear()
    releaseFetch.countDown()

    assertTrue(fetchFinished.await(3, TimeUnit.SECONDS), "in-flight fetch should finish")
    eventually("stale fetch should not repopulate the cache") {
      repository.getImage(imageUrl) == null && repository.getState(imageUrl) is ImageLoadState.NotRequested
    }
  }

  @Test
  fun testClearedInFlightRequestDoesNotReplaceNewerImageForSameUrl() {
    val calls = AtomicInteger()
    val firstFetchStarted = CountDownLatch(1)
    val releaseFirstFetch = CountDownLatch(1)
    val firstFetchFinished = CountDownLatch(1)
    val secondLoad = CountDownLatch(1)
    val repository = repository {
      if (calls.incrementAndGet() == 1) {
        firstFetchStarted.countDown()
        assertTrue(releaseFirstFetch.await(3, TimeUnit.SECONDS), "first fetch should be released")
        firstFetchFinished.countDown()
        imageResource(10, 10, "old")
      } else {
        imageResource(20, 20, "new")
      }
    }

    repository.requestImage(imageUrl) {
      fail("callbacks registered before clear should be discarded")
    }
    assertTrue(firstFetchStarted.await(3, TimeUnit.SECONDS), "first fetch should start before cache is cleared")

    repository.clear()
    repository.requestImage(imageUrl) {
      secondLoad.countDown()
    }

    assertTrue(secondLoad.await(3, TimeUnit.SECONDS), "second fetch should load after clear")
    releaseFirstFetch.countDown()
    assertTrue(firstFetchFinished.await(3, TimeUnit.SECONDS), "stale first fetch should finish")

    eventually("newer image should remain cached after the stale fetch finishes") {
      calls.get() == 2 && repository.getImage(imageUrl)?.originalWidth == 20
    }
  }

  private fun repository(
    nowProvider: () -> Long = { System.currentTimeMillis() },
    failureBackoffMs: Long = ImageRepository.DEFAULT_FAILURE_BACKOFF_MS,
    settings: ImagePreviewSettings.StateData = ImagePreviewSettings.StateData(),
    fetch: (String) -> ImageResource?,
  ): ImageRepository {
    return ImageRepository(
      loader = ImageLoader { fetch(it) },
      executor = executor,
      runOnUiThread = { it() },
      nowProvider = nowProvider,
      failureBackoffMs = failureBackoffMs,
      settingsProvider = { settings },
    )
  }

  private fun imageResource(width: Int, height: Int, size: String): ImageResource {
    return ImageResource(
      sourceUrl = imageUrl,
      previewImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB),
      imageSize = size,
      byteSize = 1,
    )
  }

  private fun eventually(message: String, condition: () -> Boolean) {
    val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3)
    while (System.nanoTime() < deadline) {
      if (condition()) {
        return
      }
      Thread.sleep(10)
    }
    assertTrue(condition(), message)
  }
}
