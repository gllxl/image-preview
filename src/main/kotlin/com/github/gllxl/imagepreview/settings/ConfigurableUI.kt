package com.github.gllxl.imagepreview.settings

import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent


class ImagePreviewConfigurableUI(setting: ImagePreviewSettings) : ConfigurableUi<ImagePreviewSettings> {

//  private var isShowPreviewIcon = AtomicProperty(setting.isShowPreviewIcon)

  private val ui: DialogPanel = panel {
    var tinyPreviewCheckbox: Cell<JBCheckBox>
    var tinyPreviewIconType: Cell<JBCheckBox>

    group ("Preview")  {
      row("") {
        tinyPreviewCheckbox = checkBox("Show preview icon")
          .align(AlignX.LEFT)
          .gap(RightGap.SMALL)
          .resizableColumn().apply {
            component.toolTipText = "If unchecked, the preview will be a thumbnail of the image"
          }
          .bindSelected(setting::isShowPreviewIcon)
          .whenStateChangedFromUi { isChecked ->
            if (isChecked) {
              return@whenStateChangedFromUi
            }
          }
      }
      row("") {
        tinyPreviewIconType = checkBox("Show actual image preview")
          .align(AlignX.LEFT)
          .gap(RightGap.SMALL)
          .resizableColumn().apply {
            component.toolTipText = "If unchecked, the icon will be a default icon"
          }
          .bindSelected(setting::isShowActualImageIcon)
          .whenStateChangedFromUi { isChecked ->
            if (isChecked) {
              return@whenStateChangedFromUi
            }
          }
      }
    }

  }

  override fun reset(settings: ImagePreviewSettings) {
    ui.reset()
  }

  override fun isModified(settings: ImagePreviewSettings): Boolean {
    return ui.isModified()
  }

  override fun apply(settings: ImagePreviewSettings) {
    ui.apply()
  }

  override fun getComponent(): JComponent {
    return ui
  }
}