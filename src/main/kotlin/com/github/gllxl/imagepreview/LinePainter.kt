package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.service.imagePreviewService
import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import com.intellij.openapi.editor.EditorLinePainter
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class LinePainter : EditorLinePainter() {
  override fun getLineExtensions(project: Project, file: VirtualFile, line: Int): Collection<LineExtensionInfo> {
    if (!ImagePreviewSettings.currentState().isShowInlineImageSize) {
      return emptyList()
    }

    val imageService = imagePreviewService(project)
    val imgUrl = imageService.references.getLineMapping(file, line) ?: return emptyList()
    val image = imageService.getImage(imgUrl)

    if (image == null) {
      imageService.requestImage(imgUrl)
      return emptyList()
    }

    val text = "   ${image.originalWidth} * ${image.originalHeight} (${image.imageSize})"
    val textAttributes = getHandlerAttributes()
    return arrayListOf(LineExtensionInfo(text, textAttributes))
  }
}
