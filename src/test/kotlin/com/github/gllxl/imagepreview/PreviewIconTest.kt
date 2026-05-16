package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.model.ImageResource
import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import com.github.gllxl.imagepreview.ui.createPreviewThumbnailIcon
import com.github.gllxl.imagepreview.ui.defaultPreviewIcon
import com.github.gllxl.imagepreview.ui.previewIconFor
import com.github.gllxl.imagepreview.ui.previewThumbnailSize
import java.awt.image.BufferedImage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class PreviewIconTest {

  @Test
  fun testThumbnailSizeFitsLandscapePortraitAndSquareImages() {
    assertEquals(Pair(16, 8), previewThumbnailSize(width = 200, height = 100))
    assertEquals(Pair(8, 16), previewThumbnailSize(width = 100, height = 200))
    assertEquals(Pair(16, 16), previewThumbnailSize(width = 32, height = 32))
  }

  @Test
  fun testThumbnailSizeUpscalesTinyImagesButPreservesAspectRatio() {
    assertEquals(Pair(16, 8), previewThumbnailSize(width = 2, height = 1))
    assertEquals(Pair(1, 1), previewThumbnailSize(width = 0, height = 0))
  }

  @Test
  fun testCreatePreviewThumbnailIconUsesScaledImageSize() {
    val icon = createPreviewThumbnailIcon(BufferedImage(120, 60, BufferedImage.TYPE_INT_ARGB))

    assertEquals(16, icon.iconWidth)
    assertEquals(8, icon.iconHeight)
  }

  @Test
  fun testPreviewIconForReturnsNullWhenPreviewIconIsDisabled() {
    val settings = ImagePreviewSettings.StateData(
      isShowPreviewIcon = false,
      isShowActualImageIcon = true,
    )

    assertNull(previewIconFor(imageResource(), settings))
  }

  @Test
  fun testPreviewIconForUsesDefaultIconBeforeImageLoads() {
    val settings = ImagePreviewSettings.StateData(
      isShowPreviewIcon = true,
      isShowActualImageIcon = true,
    )

    assertSame(defaultPreviewIcon, previewIconFor(null, settings))
  }

  @Test
  fun testPreviewIconForUsesDefaultIconWhenActualThumbnailIsDisabled() {
    val settings = ImagePreviewSettings.StateData(
      isShowPreviewIcon = true,
      isShowActualImageIcon = false,
    )

    assertSame(defaultPreviewIcon, previewIconFor(imageResource(), settings))
  }

  @Test
  fun testPreviewIconForUsesActualThumbnailWhenEnabledAndImageIsLoaded() {
    val settings = ImagePreviewSettings.StateData(
      isShowPreviewIcon = true,
      isShowActualImageIcon = true,
    )
    val icon = previewIconFor(imageResource(width = 64, height = 32), settings)

    assertTrue(icon !== defaultPreviewIcon)
    assertEquals(16, icon?.iconWidth)
    assertEquals(8, icon?.iconHeight)
  }

  private fun imageResource(width: Int = 32, height: Int = 16): ImageResource {
    return ImageResource(
      sourceUrl = "https://cdn.example.com/image.png",
      previewImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB),
      imageSize = "1 kB",
      byteSize = 1024,
    )
  }
}
