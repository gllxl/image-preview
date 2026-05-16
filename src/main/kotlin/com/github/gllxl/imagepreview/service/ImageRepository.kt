package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.isImageReference
import com.github.gllxl.imagepreview.isImageUrl
import com.github.gllxl.imagepreview.model.ImageLoadState
import com.github.gllxl.imagepreview.model.ImageResource
import com.github.gllxl.imagepreview.settings.ImagePreviewDomainPolicy
import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicLong

class ImageRepository(
  private val cache: ImageCache = ImageCache(MAX_CACHE_ENTRIES),
  private val loader: ImageLoader = RoutingImageLoader(),
  private val refreshScheduler: RefreshScheduler = RefreshScheduler(),
  private val executor: Executor = AppExecutorUtil.createBoundedApplicationPoolExecutor(
    "Image Preview Loader",
    MAX_CONCURRENT_REQUESTS,
  ),
  private val runOnUiThread: ((() -> Unit) -> Unit) = ::invokeLater,
  private val nowProvider: () -> Long = { System.currentTimeMillis() },
  private val failureBackoffMs: Long = DEFAULT_FAILURE_BACKOFF_MS,
  private val settingsProvider: () -> ImagePreviewSettings.StateData = ImagePreviewSettings::currentState,
) {
  private val cacheGeneration = AtomicLong()
  private val loadingUrls = ConcurrentHashMap<String, Long>()
  private val failedUrls = ConcurrentHashMap<String, Long>()
  private val callbacks = HashMap<String, MutableList<PendingCallback>>()

  fun getImage(imageUrl: String): ImageResource? {
    return cache.get(imageUrl)
  }

  fun getState(imageUrl: String): ImageLoadState {
    getImage(imageUrl)?.let {
      return ImageLoadState.Ready(it)
    }

    if (loadingUrls.containsKey(imageUrl)) {
      return ImageLoadState.Loading
    }

    val failedAt = failedUrls[imageUrl]
    if (failedAt != null && nowProvider() - failedAt < failureBackoffMs) {
      return ImageLoadState.Failed(failedAt)
    }
    if (failedAt != null) {
      failedUrls.remove(imageUrl, failedAt)
    }

    return ImageLoadState.NotRequested
  }

  fun requestImage(
    imageUrl: String,
    project: Project? = null,
    requestKind: ImageLoadRequestKind = ImageLoadRequestKind.AUTOMATIC,
    onLoaded: ((ImageResource) -> Unit)? = null,
  ) {
    if (!isImageReference(imageUrl) ||
      isInFailureBackoff(imageUrl) ||
      !isLoadingAllowed(imageUrl, requestKind)
    ) {
      return
    }

    getImage(imageUrl)?.let {
      if (onLoaded != null) {
        notifyOnUiThread(project, listOf(onLoaded), it)
      }
      return
    }

    val requestGeneration = cacheGeneration.get()

    if (onLoaded != null) {
      synchronized(callbacks) {
        callbacks.getOrPut(imageUrl) { mutableListOf() }.add(PendingCallback(requestGeneration, onLoaded))
      }
    }

    when (markLoading(imageUrl, requestGeneration)) {
      LoadingMark.STARTED -> Unit
      LoadingMark.ALREADY_LOADING -> return
      LoadingMark.STALE -> {
        drainCallbacks(imageUrl, requestGeneration)
        return
      }
    }

    try {
      executor.execute {
        if (isStale(requestGeneration)) {
          loadingUrls.remove(imageUrl, requestGeneration)
          drainCallbacks(imageUrl, requestGeneration)
          return@execute
        }
        loadImage(imageUrl, project, requestGeneration)
      }
    } catch (e: RejectedExecutionException) {
      logger.warn("Failed to schedule image preview load: $imageUrl", e)
      loadingUrls.remove(imageUrl, requestGeneration)
      if (!isStale(requestGeneration)) {
        rememberFailure(imageUrl)
        drainCallbacks(imageUrl, requestGeneration)
      }
    }
  }

  fun clear() {
    cacheGeneration.incrementAndGet()
    cache.clear()
    loadingUrls.clear()
    failedUrls.clear()
    synchronized(callbacks) {
      callbacks.clear()
    }
  }

  private fun loadImage(imageUrl: String, project: Project?, requestGeneration: Long) {
    try {
      val image = loader.load(imageUrl)
      if (isStale(requestGeneration)) {
        return
      }

      if (image == null) {
        rememberFailure(imageUrl)
        drainCallbacks(imageUrl, requestGeneration)
        return
      }

      cache.put(imageUrl, image)
      failedUrls.remove(imageUrl)
      notifyOnUiThread(project, drainCallbacks(imageUrl, requestGeneration), image)
    } catch (e: IOException) {
      logger.warn("Failed to load image preview: $imageUrl", e)
      if (!isStale(requestGeneration)) {
        rememberFailure(imageUrl)
        drainCallbacks(imageUrl, requestGeneration)
      }
    } catch (e: RuntimeException) {
      logger.warn("Failed to load image preview: $imageUrl", e)
      if (!isStale(requestGeneration)) {
        rememberFailure(imageUrl)
        drainCallbacks(imageUrl, requestGeneration)
      }
    } finally {
      loadingUrls.remove(imageUrl, requestGeneration)
    }
  }

  private fun drainCallbacks(imageUrl: String, requestGeneration: Long): List<(ImageResource) -> Unit> {
    return synchronized(callbacks) {
      val pendingCallbacks = callbacks[imageUrl] ?: return@synchronized emptyList()
      val matchingCallbacks = pendingCallbacks.filter { it.generation == requestGeneration }
      val remainingCallbacks = pendingCallbacks.filterTo(mutableListOf()) { it.generation != requestGeneration }

      if (remainingCallbacks.isEmpty()) {
        callbacks.remove(imageUrl)
      } else {
        callbacks[imageUrl] = remainingCallbacks
      }

      matchingCallbacks.map { it.callback }
    }
  }

  private fun isInFailureBackoff(imageUrl: String): Boolean {
    val failedAt = failedUrls[imageUrl] ?: return false
    if (nowProvider() - failedAt < failureBackoffMs) {
      return true
    }
    failedUrls.remove(imageUrl, failedAt)
    return false
  }

  private fun rememberFailure(imageUrl: String) {
    failedUrls[imageUrl] = nowProvider()
  }

  private fun markLoading(imageUrl: String, requestGeneration: Long): LoadingMark {
    while (true) {
      val currentGeneration = cacheGeneration.get()
      if (requestGeneration != currentGeneration) {
        return LoadingMark.STALE
      }

      val existingGeneration = loadingUrls.putIfAbsent(imageUrl, requestGeneration)
        ?: return LoadingMark.STARTED
      if (existingGeneration != currentGeneration) {
        loadingUrls.remove(imageUrl, existingGeneration)
        continue
      }

      return LoadingMark.ALREADY_LOADING
    }
  }

  private fun isStale(requestGeneration: Long): Boolean {
    return requestGeneration != cacheGeneration.get()
  }

  private fun isLoadingAllowed(imageUrl: String, requestKind: ImageLoadRequestKind): Boolean {
    val settings = settingsProvider()
    val remoteImage = isImageUrl(imageUrl)

    if (remoteImage && !ImagePreviewDomainPolicy.isAllowed(imageUrl, settings.allowedDomains)) {
      return false
    }

    if (requestKind == ImageLoadRequestKind.EXPLICIT) {
      return true
    }

    if (settings.isLoadOnlyWhenPreviewIsClicked) {
      return false
    }

    return if (remoteImage) {
      settings.isLoadRemoteImagesAutomatically
    } else {
      settings.isLoadLocalImagesAutomatically
    }
  }

  private fun notifyOnUiThread(
    project: Project?,
    imageCallbacks: List<(ImageResource) -> Unit>,
    image: ImageResource,
  ) {
    runOnUiThread {
      if (project?.isDisposed == true) {
        return@runOnUiThread
      }

      imageCallbacks.forEach { it(image) }
      refreshScheduler.schedule(project)
    }
  }

  companion object {
    const val MAX_CACHE_ENTRIES = 100
    const val MAX_CONCURRENT_REQUESTS = 3
    const val DEFAULT_FAILURE_BACKOFF_MS = 60_000L
    private val logger = Logger.getInstance(ImageRepository::class.java)

    private fun invokeLater(runnable: () -> Unit) {
      val application = ApplicationManager.getApplication()
      if (application == null) {
        runnable()
      } else {
        application.invokeLater(runnable)
      }
    }
  }

  private data class PendingCallback(
    val generation: Long,
    val callback: (ImageResource) -> Unit,
  )

  private enum class LoadingMark {
    STARTED,
    ALREADY_LOADING,
    STALE,
  }
}
