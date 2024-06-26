package com.github.gllxl.imagepreview.settings

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.options.ConfigurableUi
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent


class ImagePreviewConfigurableUI(setting: ImagePreviewSettings) : ConfigurableUi<ImagePreviewSettings> {

//  private var isShowPreviewIcon = AtomicProperty(setting.isShowPreviewIcon)

  private val ui: DialogPanel = panel {
    var tinyPreviewCheckbox: Cell<JBCheckBox>
    row("") {
      tinyPreviewCheckbox = checkBox("Show preview icon")
        .align(AlignX.FILL)
        .gap(RightGap.SMALL)
        .resizableColumn().apply {
          component.toolTipText = "You can put here whatever you want, $$ is the selected variable, {FP} filepath, {FN} filename, {LN} line number "
        }
        .bindSelected(setting::isShowPreviewIcon)
        .whenStateChangedFromUi { isChecked ->
          if (isChecked) {
            return@whenStateChangedFromUi
          }
        }

    }.layout(RowLayout.PARENT_GRID)
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