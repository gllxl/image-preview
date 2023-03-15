package com.github.gllxl.imagepreview

fun isImageUrl(url: String): Boolean {
  val regex = "^(http|https)://.*(jpg|jpeg|png|gif|bmp|webp)$".toRegex(RegexOption.IGNORE_CASE)
  return regex.containsMatchIn(url)
}

fun removeUrlQuotes(url: String): String {
  return url.replace("\"", "").replace("\"", "")
}
