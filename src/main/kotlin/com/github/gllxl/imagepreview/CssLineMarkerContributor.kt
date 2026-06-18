package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.extractor.CssImageReferenceExtractor
import com.github.gllxl.imagepreview.service.imagePreviewService
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.css.impl.CssElementTypes.CSS_URI
import com.intellij.psi.css.impl.CssElementTypes.CSS_URI_START
import com.intellij.psi.util.elementType

class CssLineMarkerContributor : LineMakerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    val cssUriElement = cssUriElementForLineMarker(element) ?: return null
    val rawImageReference = CssImageReferenceExtractor.extractUrl(cssUriElement) ?: return null

    val document = PsiDocumentManager.getInstance(element.project).getDocument(element.containingFile) ?: return null
    val virtualFile = element.containingFile.virtualFile ?: return null
    val imageService = imagePreviewService(element.project)
    val imageReference = imageService.resolveImageReference(rawImageReference, virtualFile) ?: return null
    imageService.references.setLineMapping(
      virtualFile,
      document,
      cssUriElement.textRange,
      rawImageReference,
      imageReference,
    )

    return getLineMaker(imageReference, element.project)
  }

}

private fun cssUriElementForLineMarker(element: PsiElement): PsiElement? {
  if (element.elementType !== CSS_URI_START) {
    return null
  }

  val parent = element.parent ?: return null
  if (parent.elementType !== CSS_URI) {
    return null
  }

  return parent
}
