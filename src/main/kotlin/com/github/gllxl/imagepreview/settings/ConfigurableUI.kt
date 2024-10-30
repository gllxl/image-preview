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
            component.toolTipText = "If checked, an icon previewing the image will be displayed in front of the code line."
          }
          .bindSelected(setting::isShowPreviewIcon)
          .whenStateChangedFromUi { isChecked ->
            if (isChecked) {
              return@whenStateChangedFromUi
            }
          }
      }
      row("") {
        tinyPreviewIconType = checkBox("Show thumbnail image")
          .align(AlignX.LEFT)
          .gap(RightGap.SMALL)
          .resizableColumn().apply {
            component.toolTipText = "If checked, a thumbnail preview icon of the image will be displayed in front of the code line. Otherwise, a default icon will be displayed"
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