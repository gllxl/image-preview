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

fun getKeyByValue(map:HashMap<Number, String>, value: String): ArrayList<Number?> {
  val keyList = ArrayList<Number?>()
  var key: Number? = null
  val set: Set<Map.Entry<Number, String>> = map.entries
  val it: Iterator<*> = set.iterator()
  while (it.hasNext()) {
    val (key1, value1) = it.next() as Map.Entry<Number, String>
    if (value1 == value) {
      key = key1
      keyList.add(key)
    }
  }
  return keyList
}