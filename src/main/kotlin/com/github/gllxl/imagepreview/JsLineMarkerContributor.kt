package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.extractor.JsImageReferenceExtractor
import com.github.gllxl.imagepreview.service.imagePreviewService
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement

class JsLineMarkerContributor : LineMakerContributor() {
  override fun getInfo(element: PsiElement): Info? {
    val rawImageReference = JsImageReferenceExtractor.extractUrl(element) ?: return null

    val document = PsiDocumentManager.getInstance(element.project).getDocument(element.containingFile) ?: return null
    val virtualFile = element.containingFile.virtualFile ?: return null
    val imageService = imagePreviewService(element.project)
    val imageReference = imageService.resolveImageReference(rawImageReference, virtualFile) ?: return null
    imageService.references.setLineMapping(
      virtualFile,
      document,
      element.textRange,
      rawImageReference,
      imageReference,
    )

    return getLineMaker(imageReference, element.project)
  }

}
