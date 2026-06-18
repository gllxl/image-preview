package com.github.gllxl.imagepreview.extractor

import com.github.gllxl.imagepreview.isImageReference
import com.github.gllxl.imagepreview.removeUrlQuotes
import com.intellij.json.psi.JsonArray
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.lang.javascript.psi.JSArrayLiteralExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.lang.javascript.psi.ecma6.JSStringTemplateExpression
import com.intellij.psi.PsiElement

object JsImageReferenceExtractor : ImageReferenceExtractor {
  override fun extractUrl(element: PsiElement): String? {
    return when (element) {
      is JSVariable -> extractJsLiteralValue(element.initializer)
      is JSProperty -> extractJsLiteralValue(element.value)
      is JSLiteralExpression -> extractJsArrayLiteralValue(element)
      is JsonProperty -> extractJsonLiteralValue(element.value)
      is JsonStringLiteral -> extractJsonArrayLiteralValue(element)
      else -> null
    }
  }

  private fun extractJsArrayLiteralValue(element: JSLiteralExpression): String? {
    if (element.parent !is JSArrayLiteralExpression) {
      return null
    }

    return extractJsLiteralValue(element)
  }

  private fun extractJsLiteralValue(element: PsiElement?): String? {
    if (element !is JSLiteralExpression) {
      return null
    }

    if (element is JSStringTemplateExpression && element.arguments.isNotEmpty()) {
      return null
    }

    val rawValue = element.stringValue
      ?: (element.value as? String)
      ?: element.text

    return normalizeImageReference(rawValue)
  }

  private fun extractJsonArrayLiteralValue(element: JsonStringLiteral): String? {
    if (element.isPropertyName || element.parent !is JsonArray) {
      return null
    }

    return normalizeImageReference(element.value)
  }

  private fun extractJsonLiteralValue(element: PsiElement?): String? {
    if (element !is JsonStringLiteral || element.isPropertyName) {
      return null
    }

    return normalizeImageReference(element.value)
  }

  internal fun normalizeImageReference(rawValue: String?): String? {
    val reference = rawValue
      ?.let(::removeUrlQuotes)
      ?.takeIf { it.isNotBlank() }
      ?: return null

    return reference.takeIf(::isImageReference)
  }
}
