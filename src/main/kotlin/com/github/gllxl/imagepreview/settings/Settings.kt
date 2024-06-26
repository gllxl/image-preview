package com.github.gllxl.imagepreview.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import org.jetbrains.annotations.Nullable

internal const val DEFAULT_IS_SHOW_PREVIEW_ICON = true


@State(name = "ImagePreviewSettings", storages = [(Storage("ImagePreview.xml"))])
class ImagePreviewSettings : PersistentStateComponent<ImagePreviewSettings> {

  var isShowPreviewIcon: Boolean = DEFAULT_IS_SHOW_PREVIEW_ICON

  var version = System.getenv("IMAGE_PREVIEW_VERSION")

  companion object {
    val instance: ImagePreviewSettings
      get() = ApplicationManager.getApplication().getService(ImagePreviewSettings::class.java)
  }

  @Nullable
  override fun getState(): ImagePreviewSettings = this

  override fun loadState(state: ImagePreviewSettings) {
    XmlSerializerUtil.copyBean(state, this)
  }
}