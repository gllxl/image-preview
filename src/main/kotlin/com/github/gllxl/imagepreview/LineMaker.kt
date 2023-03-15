package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.ui.PreviewPopup
import com.github.gllxl.imagepreview.ui.previewIcon
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.net.HttpConfigurable
import org.apache.commons.httpclient.util.HttpURLConnection
import javax.imageio.ImageIO

fun getLineMaker (imgUrl: String): RunLineMarkerContributor.Info? {
  val isImage = isImageUrl(imgUrl);

  if (!isImage) {
    return null
  }

  val config = HttpConfigurable.getInstance()
  val res = config.openHttpConnection(imgUrl)

  val image = ImageIO.read(res.inputStream)

  val previewAction = object : AnAction("Preview", "Preview", AllIcons.Debugger.Watch) {
    override fun actionPerformed(e: AnActionEvent) {

      if (res.responseCode === HttpURLConnection.HTTP_OK) {
        PreviewPopup.show(WindowManager.getInstance().suggestParentWindow(null), image)
      }
    }
  }

  return RunLineMarkerContributor.Info(previewIcon, arrayOf(previewAction)) {
    "preview image"
  }
}