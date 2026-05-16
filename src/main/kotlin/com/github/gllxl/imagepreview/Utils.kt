package com.github.gllxl.imagepreview

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Font
import java.net.URI
import java.text.DecimalFormat
import kotlin.math.min

private val supportedImageExtensions = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg")

fun isImageUrl(url: String): Boolean {
  val extension = imageUrlExtension(url) ?: return false
  return extension in supportedImageExtensions
}

fun isImageReference(reference: String): Boolean {
  val extension = imageReferenceExtension(reference) ?: return false
  return extension in supportedImageExtensions
}

fun imageUrlExtension(url: String): String? {
  return try {
    val uri = URI(url.trim())
    val scheme = uri.scheme?.lowercase()
    if (scheme != "http" && scheme != "https") {
      return null
    }

    uri.path
      ?.substringAfterLast('/', "")
      ?.substringAfterLast('.', "")
      ?.lowercase()
      ?.takeIf { it.isNotBlank() }
  } catch (e: Exception) {
    null
  }
}

fun imageReferenceExtension(reference: String): String? {
  val trimmed = reference.trim()
  if (trimmed.startsWith("//")) {
    return null
  }

  imageUrlExtension(trimmed)?.let {
    return it
  }

  if (trimmed.startsWith("http:", true) || trimmed.startsWith("https:", true)) {
    return null
  }

  if (trimmed.contains("://") && !trimmed.startsWith("file:", true)) {
    return null
  }

  if (hasUnsupportedUriScheme(trimmed)) {
    return null
  }

  return localPathExtension(trimmed)
}

private fun hasUnsupportedUriScheme(reference: String): Boolean {
  val scheme = try {
    URI(reference).scheme?.lowercase()
  } catch (e: Exception) {
    null
  } ?: return false

  if (scheme == "file") {
    return false
  }

  if (scheme.length == 1 && reference.length > 2 && reference[1] == ':' && (reference[2] == '\\' || reference[2] == '/')) {
    return false
  }

  return true
}

private fun localPathExtension(path: String): String? {
  val normalized = path
    .substringBefore('#')
    .substringBefore('?')
    .trim()
    .trimEnd('/', '\\')

  if (normalized.isBlank()) {
    return null
  }

  return normalized
    .substringAfterLast('/', normalized)
    .substringAfterLast('\\')
    .substringAfterLast('.', "")
    .lowercase()
    .takeIf { it.isNotBlank() }
}

fun removeUrlQuotes(url: String): String {
  return url.trim().trim('"', '\'', '`')
}

fun getTextColor(): Color? {
  val globalScheme = EditorColorsManager.getInstance().globalScheme

  return globalScheme.getAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT)?.foregroundColor
}

fun getHandlerAttributes(isItalic: Boolean = true) =
  TextAttributes(getTextColor(), null, null, EffectType.BOXED, if (isItalic) Font.ITALIC else Font.PLAIN)

fun readableFileSize(size: Long): String {
  if (size <= 0) return "0"
  val units = arrayOf("B", "kB", "MB", "GB", "TB")
  val digitGroups = min((Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt(), units.lastIndex)
  return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
}
