package com.github.gllxl.imagepreview

import com.intellij.psi.PsiElement

fun getVariableContent (element: PsiElement): String {
  return element.node.lastChildNode.text.replace("\'", "").replace("\"", "")
}

fun isImageUrl(url: String): Boolean {
  val regex = "^(http|https)://.*(jpg|jpeg|png|gif|bmp)$".toRegex(RegexOption.IGNORE_CASE)
  return regex.containsMatchIn(url)
}
