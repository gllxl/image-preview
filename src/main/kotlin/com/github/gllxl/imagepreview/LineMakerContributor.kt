package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.ui.PreviewPopup
import com.github.gllxl.imagepreview.ui.previewIcon
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.WindowManager

abstract class LineMakerContributor: RunLineMarkerContributor() {
  private val imagePool = ImagePool()

  fun getLineMaker (imgUrl: String): Info? {
    val image = imagePool.getImageByUrl(imgUrl) ?: return null

    val previewAction = object : AnAction("Preview", "Preview", AllIcons.Debugger.Watch) {
      override fun actionPerformed(e: AnActionEvent) {
        PreviewPopup.show(WindowManager.getInstance().suggestParentWindow(null), image)
      }
    }

    return Info(previewIcon, arrayOf(previewAction)) {
      "preview image"
    }
  }
}