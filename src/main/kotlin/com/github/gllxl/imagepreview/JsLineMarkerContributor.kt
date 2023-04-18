package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.ImageMapping.setLineMapping
import com.intellij.lang.javascript.psi.JSVariable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

fun getJsVariableContent (element: PsiElement): String {
  return element.node.lastChildNode.text
}

class JsLineMarkerContributor : LineMakerContributor() {
  override fun getInfo(element: PsiElement): Info? {

    if (element is JSVariable) {

      if (!element.firstChild.isValid) {
        return null
      }

      val document = PsiDocumentManager.getInstance(element.project).getDocument(element.containingFile) ?: return null
      val lineNumber = document.getLineNumber(element.startOffset)
      val imgUrl = getJsVariableContent(element)
      setLineMapping(element.containingFile.virtualFile, lineNumber, removeUrlQuotes(imgUrl))

      return getLineMaker(removeUrlQuotes(imgUrl))
    }
    return null
  }

}