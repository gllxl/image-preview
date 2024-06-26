package com.github.gllxl.imagepreview.settings

import com.intellij.openapi.options.ConfigurableBase

class ImagePreviewSettingsConfigurable : ConfigurableBase<ImagePreviewConfigurableUI, ImagePreviewSettings>("com.github.gllxl.imagepreview", "Image Preview", "") {

  override fun getSettings(): ImagePreviewSettings {
    return ImagePreviewSettings.instance
  }

  override fun createUi(): ImagePreviewConfigurableUI {
    return ImagePreviewConfigurableUI(settings)
  }
}