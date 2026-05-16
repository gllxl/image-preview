package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.service.ImageLoadRequestKind
import com.github.gllxl.imagepreview.service.imagePreviewService
import com.github.gllxl.imagepreview.ui.PreviewPopup
import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import com.github.gllxl.imagepreview.ui.previewIconFor
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager

abstract class LineMakerContributor : RunLineMarkerContributor() {
  fun getLineMaker(imgUrl: String, project: Project): Info? {
    if (!isImageReference(imgUrl)) {
      return null
    }

    val imageService = imagePreviewService(project)
    val cachedImage = imageService.getImage(imgUrl)
    val settings = ImagePreviewSettings.currentState()
    if (cachedImage == null) {
      imageService.requestImage(imgUrl)
    }

    val markerIcon = previewIconFor(cachedImage, settings) ?: return null

    val previewAction = object : AnAction("Preview", "Preview", AllIcons.Debugger.Watch) {
      override fun actionPerformed(e: AnActionEvent) {
        val actionProject = e.project ?: project
        val actionImageService = imagePreviewService(actionProject)
        val cachedImage = actionImageService.getImage(imgUrl)
        val popupSettings = ImagePreviewSettings.currentState()

        if (cachedImage != null) {
          PreviewPopup.show(
            WindowManager.getInstance().suggestParentWindow(actionProject),
            cachedImage.imageBuffered,
            popupSettings.isScalePopupImageToFitScreen,
          )
          return
        }

        actionImageService.requestImage(imgUrl, ImageLoadRequestKind.EXPLICIT) {
          PreviewPopup.show(
            WindowManager.getInstance().suggestParentWindow(actionProject),
            it.imageBuffered,
            ImagePreviewSettings.currentState().isScalePopupImageToFitScreen,
          )
        }
      }
    }

    return Info(markerIcon, arrayOf(previewAction)) {
      if (imageService.getImage(imgUrl) != null) "preview image" else "load image preview"
    }
  }
}
