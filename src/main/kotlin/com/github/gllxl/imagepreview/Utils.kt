package com.github.gllxl.imagepreview

import com.intellij.psi.PsiElement

fun getVariableContent (element: PsiElement): String {
  return element.node.lastChildNode.text.replace("\'", "").replace("\"", "")
}

fun isImageUrl (url: String): Boolean {
  return url.startsWith("https://") && url.endsWith(".png")
}