package com.github.gllxl.imagepreview.ui

import com.github.gllxl.imagepreview.JsLineMarkerContributor
import com.github.gllxl.imagepreview.model.ImageResource
import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import com.intellij.openapi.util.IconLoader
import com.intellij.util.IconUtil
import com.intellij.util.ui.ImageUtil
import java.awt.Image
import java.awt.image.BufferedImage
import javax.swing.Icon
import kotlin.math.min
import kotlin.math.roundToInt

private const val PREVIEW_ICON_MAX_SIDE = 16

val defaultPreviewIcon: Icon = IconLoader.getIcon("/images/image.svg", JsLineMarkerContributor::class.java)
val previewIcon: Icon = defaultPreviewIcon

fun previewIconFor(image: ImageResource?, settings: ImagePreviewSettings): Icon? {
  return previewIconFor(image, settings.snapshot())
}

internal fun previewIconFor(image: ImageResource?, settings: ImagePreviewSettings.StateData): Icon? {
  if (!settings.isShowPreviewIcon) {
    return null
  }

  if (!settings.isShowActualImageIcon || image == null) {
    return defaultPreviewIcon
  }

  return getPreviewIcon(image)
}

fun getPreviewIcon(image: ImageResource): Icon {
  return createPreviewThumbnailIcon(image.previewImage)
}

internal fun createPreviewThumbnailIcon(image: BufferedImage): Icon {
  val (width, height) = previewThumbnailSize(image.width, image.height)
  val scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
  return IconUtil.createImageIcon(ImageUtil.toBufferedImage(scaledImage))
}

internal fun previewThumbnailSize(width: Int, height: Int, maxSide: Int = PREVIEW_ICON_MAX_SIDE): Pair<Int, Int> {
  if (width <= 0 || height <= 0 || maxSide <= 0) {
    return Pair(1, 1)
  }

  val scale = min(maxSide.toDouble() / width, maxSide.toDouble() / height)
  val scaledWidth = maxOf(1, (width * scale).roundToInt())
  val scaledHeight = maxOf(1, (height * scale).roundToInt())
  return Pair(scaledWidth, scaledHeight)
}
