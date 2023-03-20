package com.github.gllxl.imagepreview.ui

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import java.awt.BorderLayout
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
      val imageIcon = ImageIcon(image)
      val width = imageIcon.iconWidth;
      val height = imageIcon.iconHeight;
      val title = "$width * $height"
      panel.add(JLabel(imageIcon), BorderLayout.CENTER)
      val r = if (window != null) window.bounds else GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration.bounds
      val point = Point((r.width - imageIcon.iconWidth) / 2, (r.height - imageIcon.iconHeight) / 2)
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
  }
}