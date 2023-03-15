package com.github.gllxl.imagepreview

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement

fun getJsVariableContent (element: PsiElement): String {
  return element.node.lastChildNode.text.replace("\'", "").replace("\"", "")
}

class JsLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {

    if (element is JSVariable) {
      val firstChild = element.firstChild
      if (!firstChild.isValid) {
        return null
      }

      val value = getJsVariableContent(element)

      return getLineMaker(value)
    }
    return null
  }

}