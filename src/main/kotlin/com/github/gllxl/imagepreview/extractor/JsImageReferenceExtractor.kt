package com.github.gllxl.imagepreview.extractor

import com.github.gllxl.imagepreview.removeUrlQuotes
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiElement

object JsImageReferenceExtractor : ImageReferenceExtractor {
  override fun extractUrl(element: PsiElement): String? {
    if (element !is JSVariable || element.firstChild?.isValid != true) {
      return null
    }

    val rawValue = element.node.lastChildNode?.text ?: return null
    return removeUrlQuotes(rawValue)
  }
}
