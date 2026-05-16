package com.github.gllxl.imagepreview.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "ImagePreviewSettings", storages = [Storage("ImagePreview.xml")])
class ImagePreviewSettings : PersistentStateComponent<ImagePreviewSettings.StateData> {
  private var state = StateData()

  var isShowPreviewIcon: Boolean
    get() = state.isShowPreviewIcon
    set(value) {
      state.isShowPreviewIcon = value
    }

  var isShowActualImageIcon: Boolean
    get() = state.isShowActualImageIcon
    set(value) {
      state.isShowActualImageIcon = value
    }

  var isShowInlineImageSize: Boolean
    get() = state.isShowInlineImageSize
    set(value) {
      state.isShowInlineImageSize = value
    }

  var isLoadRemoteImagesAutomatically: Boolean
    get() = state.isLoadRemoteImagesAutomatically
    set(value) {
      state.isLoadRemoteImagesAutomatically = value
    }

  var isLoadLocalImagesAutomatically: Boolean
    get() = state.isLoadLocalImagesAutomatically
    set(value) {
      state.isLoadLocalImagesAutomatically = value
    }

  var isLoadOnlyWhenPreviewIsClicked: Boolean
    get() = state.isLoadOnlyWhenPreviewIsClicked
    set(value) {
      state.isLoadOnlyWhenPreviewIsClicked = value
    }

  var maximumImageSizeMb: Int
    get() = state.maximumImageSizeMb.coerceIn(MINIMUM_IMAGE_SIZE_MB, MAXIMUM_IMAGE_SIZE_MB)
    set(value) {
      state.maximumImageSizeMb = value.coerceIn(MINIMUM_IMAGE_SIZE_MB, MAXIMUM_IMAGE_SIZE_MB)
    }

  var isScalePopupImageToFitScreen: Boolean
    get() = state.isScalePopupImageToFitScreen
    set(value) {
      state.isScalePopupImageToFitScreen = value
    }

  var allowedDomains: String
    get() = state.allowedDomains
    set(value) {
      state.allowedDomains = value
    }

  fun snapshot(): StateData {
    return state.copy()
  }

  override fun getState(): StateData {
    return state
  }

  override fun loadState(state: StateData) {
    this.state = state.copy(
      maximumImageSizeMb = state.maximumImageSizeMb.coerceIn(MINIMUM_IMAGE_SIZE_MB, MAXIMUM_IMAGE_SIZE_MB),
    )
  }

  data class StateData(
    var isShowPreviewIcon: Boolean = true,
    var isShowActualImageIcon: Boolean = true,
    var isShowInlineImageSize: Boolean = true,
    var isLoadRemoteImagesAutomatically: Boolean = true,
    var isLoadLocalImagesAutomatically: Boolean = true,
    var isLoadOnlyWhenPreviewIsClicked: Boolean = false,
    var maximumImageSizeMb: Int = DEFAULT_MAXIMUM_IMAGE_SIZE_MB,
    var isScalePopupImageToFitScreen: Boolean = true,
    var allowedDomains: String = "",
  ) {
    val maximumImageBytes: Long
      get() = maximumImageSizeMb
        .coerceIn(MINIMUM_IMAGE_SIZE_MB, MAXIMUM_IMAGE_SIZE_MB)
        .toLong() * BYTES_PER_MEGABYTE
  }

  companion object {
    const val DEFAULT_MAXIMUM_IMAGE_SIZE_MB = 10
    const val MINIMUM_IMAGE_SIZE_MB = 1
    const val MAXIMUM_IMAGE_SIZE_MB = 100
    private const val BYTES_PER_MEGABYTE = 1024L * 1024L

    val instance: ImagePreviewSettings
      get() = ApplicationManager.getApplication().getService(ImagePreviewSettings::class.java)

    fun currentState(): StateData {
      return runCatching {
        ApplicationManager.getApplication()?.getService(ImagePreviewSettings::class.java)?.snapshot()
      }.getOrNull() ?: StateData()
    }
  }
}
