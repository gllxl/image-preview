package com.github.gllxl.imagepreview

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.intellij.psi.css.impl.CssElementTypes.*
import com.intellij.psi.util.elementType

fun getCSSBackgroundVariableContent (element: PsiElement): String? {
  return element.children.find { it.elementType === CSS_STRING }?.text
}

class CssLineMarkerContributor : RunLineMarkerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    if (element.elementType === CSS_URI) {
      val imageUrl = getCSSBackgroundVariableContent(element)

      if (imageUrl != null ) {
        return getLineMaker(removeUrlQuotes(imageUrl))
      }
      return null
    }
    return null
  }

}