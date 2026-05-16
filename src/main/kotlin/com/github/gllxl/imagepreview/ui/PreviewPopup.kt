package com.github.gllxl.imagepreview.ui

import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.Point
import java.awt.Rectangle
import java.awt.Window
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class PreviewPopup {

  companion object {

    fun show(
      window: Window?,
      image: BufferedImage,
      scaleToFit: Boolean = ImagePreviewSettings.currentState().isScalePopupImageToFitScreen,
    ) {
      val bounds = popupBounds(window)
      val displaySize = popupImageSize(image.width, image.height, bounds, scaleToFit)
      val panel = JPanel(BorderLayout())
      val imageIcon = ImageIcon(displayImage(image, displaySize))
      val width = image.width
      val height = image.height
      val title = "$width * $height"
      panel.add(JLabel(imageIcon), BorderLayout.CENTER)
      val point = popupLocation(window, bounds, displaySize)
      val location = if (window != null) RelativePoint(window, point) else RelativePoint(point)
      JBPopupFactory.getInstance().createComponentPopupBuilder(panel, panel)
        .setTitle(title)
        .setRequestFocus(true)
        .setFocusable(true)
        .setResizable(false)
        .setMovable(true)
        .setModalContext(false)
        .setShowShadow(true)
        .setShowBorder(false)
        .setCancelKeyEnabled(true)
        .setCancelOnClickOutside(true)
        .setCancelOnOtherWindowOpen(true)
        .createPopup()
        .show(location)
    }

    private fun popupBounds(window: Window?): Rectangle {
      return window?.bounds
        ?: GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
    }

    private fun popupLocation(window: Window?, bounds: Rectangle, displaySize: Dimension): Point {
      val x = max(0, (bounds.width - displaySize.width) / 2)
      val y = max(0, (bounds.height - displaySize.height) / 2)
      if (window != null) {
        return Point(x, y)
      }

      return Point(bounds.x + x, bounds.y + y)
    }

    private fun displayImage(image: BufferedImage, displaySize: Dimension): Image {
      if (displaySize.width == image.width && displaySize.height == image.height) {
        return image
      }

      return image.getScaledInstance(displaySize.width, displaySize.height, Image.SCALE_SMOOTH)
    }
  }
}

internal fun popupImageSize(
  imageWidth: Int,
  imageHeight: Int,
  bounds: Rectangle,
  scaleToFit: Boolean,
): Dimension {
  val safeWidth = max(1, imageWidth)
  val safeHeight = max(1, imageHeight)
  if (!scaleToFit) {
    return Dimension(safeWidth, safeHeight)
  }

  val maxWidth = max(1, bounds.width - POPUP_HORIZONTAL_MARGIN)
  val maxHeight = max(1, bounds.height - POPUP_VERTICAL_MARGIN)
  val scale = min(1.0, min(maxWidth.toDouble() / safeWidth, maxHeight.toDouble() / safeHeight))
  return Dimension(
    max(1, (safeWidth * scale).roundToInt()),
    max(1, (safeHeight * scale).roundToInt()),
  )
}

private const val POPUP_HORIZONTAL_MARGIN = 80
private const val POPUP_VERTICAL_MARGIN = 120
