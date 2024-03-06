package com.github.gllxl.imagepreview.ui

import com.github.gllxl.imagepreview.JsLineMarkerContributor
import com.github.gllxl.imagepreview.dto.ImageDTO
import com.intellij.openapi.util.IconLoader
import com.intellij.util.IconUtil
import com.intellij.util.ui.ImageUtil
import javax.swing.Icon

val defaultPreviewIcon = IconLoader.getIcon("/images/image.svg", JsLineMarkerContributor::class.java)

fun getPreviewIcon (image: ImageDTO): Icon {
  val tinyImage = image.imageBuffered.getScaledInstance(36, -1, 16)

  val icon = IconUtil.toRetinaAwareIcon(ImageUtil.toBufferedImage(tinyImage))
  return icon
}