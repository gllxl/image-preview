package com.github.gllxl.imagepreview


import com.github.gllxl.imagepreview.ImagePool.getImageByUrl
import com.intellij.openapi.editor.EditorLinePainter
import com.intellij.openapi.editor.LineExtensionInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.awt.image.BufferedImage
import javax.swing.ImageIcon

class LinePainter : EditorLinePainter() {
  override fun getLineExtensions(project: Project, file: VirtualFile, line: Int): Collection<LineExtensionInfo> {

    var text = "   "

    val imgUrl = ImageMapping.getLineMapping(file, line)

    if (imgUrl is String) {
      val targetImage = getImageByUrl(imgUrl)?.imageBuffered
      val targetImageSize = getImageByUrl(imgUrl)?.imageSize
      if (targetImage is BufferedImage) {
        val imageIcon = ImageIcon(targetImage)
        val width = imageIcon.iconWidth
        val height = imageIcon.iconHeight
        text += "$width * $height ($targetImageSize)"
      }
    }
    val textAttributes = getHandlerAttributes()
    return arrayListOf(LineExtensionInfo(text, textAttributes))
  }


}