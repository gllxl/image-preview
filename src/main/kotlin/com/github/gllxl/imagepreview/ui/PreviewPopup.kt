package com.github.gllxl.imagepreview.ui

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Window
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JPanel

class PreviewPopup {

  companion object {

    fun show(window: Window?, image: BufferedImage) {
      val panel = JPanel()
      val image = ImageIcon(image)
      val width = image.iconWidth;
      val height = image.iconHeight;
      val title = "$width * $height"
      panel.add(JLabel(image))
      val r = if (window != null) window.bounds else GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
      val point = Point((r.width - image.iconWidth) / 2, (r.height - image.iconHeight) / 2)
      val location = if (window != null) RelativePoint(window, point) else RelativePoint(point)
      JBPopupFactory.getInstance().createComponentPopupBuilder(panel, panel)
        .setTitle(title)
        .setRequestFocus(true)
        .setFocusable(true)
        .setResizable(false)
        .setMovable(false)
        .setModalContext(false)
        .setShowShadow(true)
        .setShowBorder(false)
        .setCancelKeyEnabled(true)
        .setCancelOnClickOutside(true)
        .setCancelOnOtherWindowOpen(true)
        .createPopup()
        .show(location)
    }
  }
}