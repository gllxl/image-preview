package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImagePreviewSettingsTest {

  @Test
  fun testDefaultsMatchFeatureBranchBehavior() {
    val settings = ImagePreviewSettings()

    assertTrue(settings.isShowPreviewIcon)
    assertTrue(settings.isShowActualImageIcon)
    assertTrue(settings.isShowInlineImageSize)
    assertTrue(settings.isLoadRemoteImagesAutomatically)
    assertTrue(settings.isLoadLocalImagesAutomatically)
    assertFalse(settings.isLoadOnlyWhenPreviewIsClicked)
    assertEquals(ImagePreviewSettings.DEFAULT_MAXIMUM_IMAGE_SIZE_MB, settings.maximumImageSizeMb)
    assertTrue(settings.isScalePopupImageToFitScreen)
    assertEquals("", settings.allowedDomains)

    assertTrue(settings.state.isShowPreviewIcon)
    assertTrue(settings.state.isShowActualImageIcon)
    assertTrue(settings.state.isShowInlineImageSize)
    assertTrue(settings.state.isLoadRemoteImagesAutomatically)
    assertTrue(settings.state.isLoadLocalImagesAutomatically)
    assertFalse(settings.state.isLoadOnlyWhenPreviewIsClicked)
    assertEquals(ImagePreviewSettings.DEFAULT_MAXIMUM_IMAGE_SIZE_MB, settings.state.maximumImageSizeMb)
    assertTrue(settings.state.isScalePopupImageToFitScreen)
    assertEquals("", settings.state.allowedDomains)
  }

  @Test
  fun testMutablePropertiesUpdatePersistentState() {
    val settings = ImagePreviewSettings()

    settings.isShowPreviewIcon = false
    settings.isShowActualImageIcon = false
    settings.isShowInlineImageSize = false
    settings.isLoadRemoteImagesAutomatically = false
    settings.isLoadLocalImagesAutomatically = false
    settings.isLoadOnlyWhenPreviewIsClicked = true
    settings.maximumImageSizeMb = 25
    settings.isScalePopupImageToFitScreen = false
    settings.allowedDomains = "cdn.example.com"

    assertFalse(settings.state.isShowPreviewIcon)
    assertFalse(settings.state.isShowActualImageIcon)
    assertFalse(settings.state.isShowInlineImageSize)
    assertFalse(settings.state.isLoadRemoteImagesAutomatically)
    assertFalse(settings.state.isLoadLocalImagesAutomatically)
    assertTrue(settings.state.isLoadOnlyWhenPreviewIsClicked)
    assertEquals(25, settings.state.maximumImageSizeMb)
    assertFalse(settings.state.isScalePopupImageToFitScreen)
    assertEquals("cdn.example.com", settings.state.allowedDomains)
  }

  @Test
  fun testLoadStateCopiesValuesWithoutSharingStateObject() {
    val settings = ImagePreviewSettings()
    val loadedState = ImagePreviewSettings.StateData(
      isShowPreviewIcon = false,
      isShowActualImageIcon = false,
      isShowInlineImageSize = false,
      isLoadRemoteImagesAutomatically = false,
      isLoadLocalImagesAutomatically = false,
      isLoadOnlyWhenPreviewIsClicked = true,
      maximumImageSizeMb = 42,
      isScalePopupImageToFitScreen = false,
      allowedDomains = "images.example.com",
    )

    settings.loadState(loadedState)
    loadedState.isShowPreviewIcon = true
    loadedState.isShowActualImageIcon = true
    loadedState.isShowInlineImageSize = true
    loadedState.isLoadRemoteImagesAutomatically = true
    loadedState.isLoadLocalImagesAutomatically = true
    loadedState.isLoadOnlyWhenPreviewIsClicked = false
    loadedState.maximumImageSizeMb = 7
    loadedState.isScalePopupImageToFitScreen = true
    loadedState.allowedDomains = "other.example.com"

    assertFalse(settings.isShowPreviewIcon)
    assertFalse(settings.isShowActualImageIcon)
    assertFalse(settings.isShowInlineImageSize)
    assertFalse(settings.isLoadRemoteImagesAutomatically)
    assertFalse(settings.isLoadLocalImagesAutomatically)
    assertTrue(settings.isLoadOnlyWhenPreviewIsClicked)
    assertEquals(42, settings.maximumImageSizeMb)
    assertFalse(settings.isScalePopupImageToFitScreen)
    assertEquals("images.example.com", settings.allowedDomains)
  }

  @Test
  fun testSnapshotCannotMutateSettings() {
    val settings = ImagePreviewSettings()
    settings.isShowPreviewIcon = false

    val snapshot = settings.snapshot()
    snapshot.isShowPreviewIcon = true

    assertFalse(settings.isShowPreviewIcon)
  }

  @Test
  fun testMaximumImageSizeIsClampedAndConvertedToBytes() {
    val settings = ImagePreviewSettings()

    settings.maximumImageSizeMb = 0
    assertEquals(ImagePreviewSettings.MINIMUM_IMAGE_SIZE_MB, settings.maximumImageSizeMb)
    assertEquals(1024L * 1024L, settings.snapshot().maximumImageBytes)

    settings.maximumImageSizeMb = ImagePreviewSettings.MAXIMUM_IMAGE_SIZE_MB + 10
    assertEquals(ImagePreviewSettings.MAXIMUM_IMAGE_SIZE_MB, settings.maximumImageSizeMb)
    assertEquals(
      ImagePreviewSettings.MAXIMUM_IMAGE_SIZE_MB.toLong() * 1024L * 1024L,
      settings.snapshot().maximumImageBytes,
    )
  }

  @Test
  fun testLoadStateClampsMaximumImageSize() {
    val settings = ImagePreviewSettings()

    settings.loadState(ImagePreviewSettings.StateData(maximumImageSizeMb = -10))
    assertEquals(ImagePreviewSettings.MINIMUM_IMAGE_SIZE_MB, settings.maximumImageSizeMb)

    settings.loadState(ImagePreviewSettings.StateData(maximumImageSizeMb = 1_000))
    assertEquals(ImagePreviewSettings.MAXIMUM_IMAGE_SIZE_MB, settings.maximumImageSizeMb)
  }
}
