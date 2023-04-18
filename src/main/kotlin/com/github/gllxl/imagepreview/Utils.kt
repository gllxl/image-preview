package com.github.gllxl.imagepreview

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font

fun isImageUrl(url: String): Boolean {
  val regex = "^(http|https)://.*(jpg|jpeg|png|gif|bmp|webp)$".toRegex(RegexOption.IGNORE_CASE)
  return regex.containsMatchIn(url)
}

fun removeUrlQuotes(url: String): String {
  return url.replace("\"", "").replace("\'", "")
}

fun getTextColor(): Color? {
  val globalScheme = EditorColorsManager.getInstance().globalScheme

  return globalScheme.getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)?.foregroundColor
}
fun getHandlerAttributes(isItalic: Boolean = true) =
  TextAttributes(getTextColor(), null, null, EffectType.BOXED, if (isItalic) Font.ITALIC else Font.PLAIN)
