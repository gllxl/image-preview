package com.github.gllxl.imagepreview.extractor

import com.intellij.psi.PsiElement

fun interface ImageReferenceExtractor {
  fun extractUrl(element: PsiElement): String?
}
