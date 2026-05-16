package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.model.ImageResource
import com.github.gllxl.imagepreview.service.ImageCache
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ImageCacheTest {

  @Test
  fun testCacheUsesLeastRecentlyUsedEviction() {
    val cache = ImageCache(maxEntries = 2)
    val first = imageResource("https://cdn.example.com/first.png")
    val second = imageResource("https://cdn.example.com/second.png")
    val third = imageResource("https://cdn.example.com/third.png")

    cache.put(first.sourceUrl, first)
    cache.put(second.sourceUrl, second)
    assertEquals(first, cache.get(first.sourceUrl))
    cache.put(third.sourceUrl, third)

    assertNotNull(cache.get(first.sourceUrl))
    assertNull(cache.get(second.sourceUrl))
    assertNotNull(cache.get(third.sourceUrl))
    assertEquals(2, cache.size())
  }

  @Test
  fun testClearEmptiesCache() {
    val cache = ImageCache(maxEntries = 2)
    val image = imageResource("https://cdn.example.com/image.png")

    cache.put(image.sourceUrl, image)
    cache.clear()

    assertNull(cache.get(image.sourceUrl))
    assertEquals(0, cache.size())
  }

  private fun imageResource(imageUrl: String): ImageResource {
    return ImageResource(
      sourceUrl = imageUrl,
      previewImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
      imageSize = "1 B",
      byteSize = 1,
    )
  }
}
