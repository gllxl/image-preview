package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.ui.popupImageSize
import java.awt.Rectangle
import kotlin.test.Test
import kotlin.test.assertEquals

class PreviewPopupTest {

  @Test
  fun testPopupImageSizeLeavesSmallImagesAtOriginalSize() {
    val size = popupImageSize(
      imageWidth = 320,
      imageHeight = 180,
      bounds = Rectangle(0, 0, 1440, 900),
      scaleToFit = true,
    )

    assertEquals(320, size.width)
    assertEquals(180, size.height)
  }

  @Test
  fun testPopupImageSizeScalesLargeImagesToFitAvailableBounds() {
    val size = popupImageSize(
      imageWidth = 2000,
      imageHeight = 1000,
      bounds = Rectangle(0, 0, 1000, 700),
      scaleToFit = true,
    )

    assertEquals(920, size.width)
    assertEquals(460, size.height)
  }

  @Test
  fun testPopupImageSizeUsesHeightAsLimitingDimension() {
    val size = popupImageSize(
      imageWidth = 1000,
      imageHeight = 2000,
      bounds = Rectangle(0, 0, 1000, 700),
      scaleToFit = true,
    )

    assertEquals(290, size.width)
    assertEquals(580, size.height)
  }

  @Test
  fun testPopupImageSizeCanDisableScaling() {
    val size = popupImageSize(
      imageWidth = 2000,
      imageHeight = 1000,
      bounds = Rectangle(0, 0, 1000, 700),
      scaleToFit = false,
    )

    assertEquals(2000, size.width)
    assertEquals(1000, size.height)
  }

  @Test
  fun testPopupImageSizeNeverReturnsZeroDimensions() {
    val size = popupImageSize(
      imageWidth = 0,
      imageHeight = 0,
      bounds = Rectangle(0, 0, 1, 1),
      scaleToFit = true,
    )

    assertEquals(1, size.width)
    assertEquals(1, size.height)
  }
}
