package com.github.gllxl.imagepreview.extractor

import com.github.gllxl.imagepreview.removeUrlQuotes
import com.intellij.psi.PsiElement
import com.intellij.psi.css.impl.CssElementTypes.CSS_STRING
import com.intellij.psi.css.impl.CssElementTypes.CSS_TERM
import com.intellij.psi.css.impl.CssElementTypes.CSS_URI
import com.intellij.psi.util.elementType

object CssImageReferenceExtractor : ImageReferenceExtractor {
  override fun extractUrl(element: PsiElement): String? {
    if (element.elementType !== CSS_URI) {
      return null
    }

    return findCssImageString(element)?.let(::removeUrlQuotes)
      ?: findUrlFunctionContent(element.text)?.let(::removeUrlQuotes)
  }

  internal fun findCssImageString(element: PsiElement): String? {
    val directString = element.children.find { it.elementType === CSS_STRING }?.text
    if (directString != null) {
      return directString
    }

    val cssTerm = element.children.find { it.elementType === CSS_TERM } ?: return null
    return cssTerm.children.find { it.elementType === CSS_STRING }?.text
  }

  internal fun findUrlFunctionContent(text: String): String? {
    val trimmed = text.trim()
    if (!trimmed.startsWith("url(", ignoreCase = true) || !trimmed.endsWith(")")) {
      return null
    }

    return trimmed
      .substringAfter('(')
      .substringBeforeLast(')')
      .trim()
      .takeIf { it.isNotBlank() }
  }
}
