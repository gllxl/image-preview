package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.isImageReference
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import java.util.WeakHashMap
import kotlin.math.abs

class ImageReferenceStore {
  private val references = WeakHashMap<VirtualFile, MutableList<ImageReferenceMapping>>()

  fun setLineMapping(file: VirtualFile, lineNumber: Int, imageUrl: String) {
    synchronized(references) {
      val mappings = references.getOrPut(file) { mutableListOf() }
      removeObsoleteMappings(mappings)

      if (!isImageReference(imageUrl)) {
        removeMappings(mappings) { it.lineNumber == lineNumber && it.document == null }
        return
      }

      val existing = mappings.firstOrNull { it.lineNumber == lineNumber && it.document == null }
      if (existing != null) {
        existing.imageUrl = imageUrl
      } else {
        mappings.add(ImageReferenceMapping(imageUrl = imageUrl, lineNumber = lineNumber))
      }
    }
  }

  fun setLineMapping(
    file: VirtualFile,
    document: Document,
    textRange: TextRange,
    rawImageReference: String,
    imageUrl: String,
  ) {
    synchronized(references) {
      val mappings = references.getOrPut(file) { mutableListOf() }
      removeObsoleteMappings(mappings)

      val startOffset = textRange.startOffset.coerceIn(0, document.textLength)
      val endOffset = textRange.endOffset.coerceIn(startOffset, document.textLength)

      if (!isImageReference(imageUrl) || startOffset == endOffset) {
        removeMappings(mappings) { it.tracks(document, startOffset, endOffset) }
        return
      }

      val referenceRange = referenceRangeInDocument(
        document = document,
        textRange = TextRange(startOffset, endOffset),
        rawImageReference = rawImageReference,
      )
      val existing = mappings.firstOrNull {
        it.tracks(document, referenceRange.startOffset, referenceRange.endOffset)
      }
      if (existing != null) {
        existing.imageUrl = imageUrl
        existing.rawImageReference = rawImageReference
        return
      }

      mappings.add(
        ImageReferenceMapping(
          imageUrl = imageUrl,
          document = document,
          startOffset = referenceRange.startOffset,
          endOffset = referenceRange.endOffset,
          rawImageReference = rawImageReference,
        ),
      )
    }
  }

  fun getLineMapping(file: VirtualFile, lineNumber: Int): String? {
    return synchronized(references) {
      val mappings = references[file] ?: return@synchronized null
      removeObsoleteMappings(mappings)
      mappings.firstOrNull { it.currentLineNumber() == lineNumber }?.imageUrl
    }
  }

  fun clear() {
    synchronized(references) {
      references.clear()
    }
  }

  private fun referenceRangeInDocument(
    document: Document,
    textRange: TextRange,
    rawImageReference: String,
  ): TextRange {
    val text = document.charsSequence
    val rangeText = text.subSequence(textRange.startOffset, textRange.endOffset)
    val referenceOffsetInRange = rangeText.indexOf(rawImageReference)
    if (referenceOffsetInRange < 0) {
      return textRange
    }

    val referenceStartOffset = textRange.startOffset + referenceOffsetInRange
    return TextRange(referenceStartOffset, referenceStartOffset + rawImageReference.length)
  }

  private fun removeObsoleteMappings(mappings: MutableList<ImageReferenceMapping>) {
    removeMappings(mappings) { !it.isCurrent() }
  }

  private fun removeMappings(
    mappings: MutableList<ImageReferenceMapping>,
    predicate: (ImageReferenceMapping) -> Boolean,
  ) {
    val iterator = mappings.iterator()
    while (iterator.hasNext()) {
      val mapping = iterator.next()
      if (predicate(mapping)) {
        iterator.remove()
      }
    }
  }

  private class ImageReferenceMapping(
    var imageUrl: String,
    val lineNumber: Int? = null,
    val document: Document? = null,
    var startOffset: Int? = null,
    var endOffset: Int? = null,
    var rawImageReference: String? = null,
  ) {
    fun currentLineNumber(): Int? {
      val document = document ?: return lineNumber
      val range = currentReferenceRange() ?: return null
      return document.getLineNumber((range.endOffset - 1).coerceAtLeast(range.startOffset))
    }

    fun isCurrent(): Boolean {
      if (document == null) {
        return true
      }

      return currentReferenceRange() != null
    }

    private fun currentReferenceRange(): TextRange? {
      val document = document ?: return null
      val text = document.charsSequence
      val currentStartOffset = startOffset
      val currentEndOffset = endOffset
      val rawReference = rawImageReference?.takeIf { it.isNotBlank() }

      if (
        currentStartOffset != null &&
        currentEndOffset != null &&
        currentStartOffset in 0..text.length &&
        currentEndOffset in currentStartOffset..text.length
      ) {
        if (rawReference == null) {
          return TextRange(currentStartOffset, currentEndOffset)
        }

        val currentText = text.subSequence(currentStartOffset, currentEndOffset)
        val referenceOffsetInRange = currentText.indexOf(rawReference)
        if (referenceOffsetInRange >= 0) {
          val referenceStartOffset = currentStartOffset + referenceOffsetInRange
          updateRange(referenceStartOffset, referenceStartOffset + rawReference.length)
          return TextRange(startOffset!!, endOffset!!)
        }
      }

      if (rawReference == null) {
        return null
      }

      val referenceStartOffset = text.nearestIndexOf(rawReference, currentStartOffset ?: 0) ?: return null
      updateRange(referenceStartOffset, referenceStartOffset + rawReference.length)
      return TextRange(startOffset!!, endOffset!!)
    }

    fun tracks(document: Document, startOffset: Int, endOffset: Int): Boolean {
      return this.document == document &&
        this.startOffset == startOffset &&
        this.endOffset == endOffset
    }

    private fun updateRange(startOffset: Int, endOffset: Int) {
      this.startOffset = startOffset
      this.endOffset = endOffset
    }
  }
}

private fun CharSequence.nearestIndexOf(target: String, preferredIndex: Int): Int? {
  if (target.length == 0) {
    return null
  }

  var bestIndex: Int? = null
  var bestDistance = Int.MAX_VALUE
  var index = indexOf(target, startIndex = 0)

  while (index >= 0) {
    val distance = abs(index - preferredIndex)
    if (distance < bestDistance) {
      bestIndex = index
      bestDistance = distance
    }
    index = indexOf(target, index + 1)
  }

  return bestIndex
}
