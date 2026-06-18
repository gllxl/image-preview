package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.service.ImageReferenceStore
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.testFramework.LightVirtualFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageReferenceStoreTest {

  @Test
  fun testStoresAndUpdatesLineMapping() {
    val store = ImageReferenceStore()
    val file = LightVirtualFile("index.js")
    val firstUrl = "https://cdn.example.com/first.png"
    val secondUrl = "https://cdn.example.com/second.png"

    store.setLineMapping(file, 8, firstUrl)
    assertEquals(firstUrl, store.getLineMapping(file, 8))

    store.setLineMapping(file, 8, secondUrl)
    assertEquals(secondUrl, store.getLineMapping(file, 8))
  }

  @Test
  fun testKeepsSameUrlOnMultipleLines() {
    val store = ImageReferenceStore()
    val file = LightVirtualFile("duplicate.js")
    val imageUrl = "https://cdn.example.com/shared.webp"

    store.setLineMapping(file, 12, imageUrl)
    store.setLineMapping(file, 35, imageUrl)

    assertEquals(imageUrl, store.getLineMapping(file, 12))
    assertEquals(imageUrl, store.getLineMapping(file, 35))
  }

  @Test
  fun testInvalidUrlClearsExistingLineMapping() {
    val store = ImageReferenceStore()
    val file = LightVirtualFile("invalid.js")

    store.setLineMapping(file, 3, "https://cdn.example.com/logo.svg")
    store.setLineMapping(file, 3, "const value = 1")

    assertNull(store.getLineMapping(file, 3))
  }

  @Test
  fun testMappingsAreScopedByVirtualFile() {
    val store = ImageReferenceStore()
    val firstFile = LightVirtualFile("first.css")
    val secondFile = LightVirtualFile("second.css")
    val firstUrl = "https://cdn.example.com/first.png"
    val secondUrl = "https://cdn.example.com/second.png"

    store.setLineMapping(firstFile, 1, firstUrl)
    store.setLineMapping(secondFile, 1, secondUrl)

    assertEquals(firstUrl, store.getLineMapping(firstFile, 1))
    assertEquals(secondUrl, store.getLineMapping(secondFile, 1))
  }

  @Test
  fun testTrackedMappingFollowsInsertedLines() {
    val store = ImageReferenceStore()
    val file = LightVirtualFile("style.css")
    val imageUrl = "https://cdn.example.com/logo.png"
    val text = """
      .error-text {
        background-image: url("$imageUrl");
      }
    """.trimIndent()
    val document = MutableTestDocument(text)
    val urlStart = text.indexOf("url(")
    val urlEnd = text.indexOf(");") + 1

    store.setLineMapping(file, document, TextRange(urlStart, urlEnd), imageUrl, imageUrl)

    assertEquals(imageUrl, store.getLineMapping(file, 1))

    document.insertString(0, ".banner {}\n\n")

    assertNull(store.getLineMapping(file, 1))
    assertEquals(imageUrl, store.getLineMapping(file, 3))
  }

  @Test
  fun testTrackedMappingIsClearedWhenReferenceTextChanges() {
    val store = ImageReferenceStore()
    val file = LightVirtualFile("style.css")
    val imageUrl = "https://cdn.example.com/logo.png"
    val text = """
      .error-text {
        background-image: url("$imageUrl");
      }
    """.trimIndent()
    val document = MutableTestDocument(text)
    val urlStart = text.indexOf("url(")
    val urlEnd = text.indexOf(");") + 1

    store.setLineMapping(file, document, TextRange(urlStart, urlEnd), imageUrl, imageUrl)
    document.replaceString(urlStart, urlEnd, "none")

    assertNull(store.getLineMapping(file, 1))
  }

  @Test
  fun testTrackedMappingUsesReferenceEndLine() {
    val store = ImageReferenceStore()
    val file = LightVirtualFile("index.js")
    val imageUrl = "https://cdn.example.com/logo.png"
    val text = """
      const logo =
        "$imageUrl";
    """.trimIndent()
    val document = MutableTestDocument(text)

    store.setLineMapping(file, document, TextRange(0, text.length), imageUrl, imageUrl)

    assertNull(store.getLineMapping(file, 0))
    assertEquals(imageUrl, store.getLineMapping(file, 1))
  }

  @Test
  fun testClearRemovesAllMappings() {
    val store = ImageReferenceStore()
    val file = LightVirtualFile("index.css")
    val imageUrl = "https://cdn.example.com/logo.png"

    store.setLineMapping(file, 1, imageUrl)
    store.clear()

    assertNull(store.getLineMapping(file, 1))
  }

  private class MutableTestDocument(initialText: String) : UserDataHolderBase(), Document {
    private val text = StringBuilder(initialText)
    private var modificationStamp = 0L

    override fun getImmutableCharSequence(): CharSequence = text

    override fun getLineCount(): Int = text.count { it == '\n' } + 1

    override fun getLineNumber(offset: Int): Int {
      val safeOffset = offset.coerceIn(0, text.length)
      var lineNumber = 0
      for (index in 0 until safeOffset) {
        if (text[index] == '\n') {
          lineNumber++
        }
      }
      return lineNumber
    }

    override fun getLineStartOffset(line: Int): Int {
      require(line in 0 until lineCount)
      if (line == 0) {
        return 0
      }

      var currentLine = 0
      text.forEachIndexed { index, character ->
        if (character == '\n') {
          currentLine++
          if (currentLine == line) {
            return index + 1
          }
        }
      }

      return text.length
    }

    override fun getLineEndOffset(line: Int): Int {
      require(line in 0 until lineCount)
      val startOffset = getLineStartOffset(line)
      for (index in startOffset until text.length) {
        if (text[index] == '\n') {
          return index
        }
      }
      return text.length
    }

    override fun insertString(offset: Int, s: CharSequence) {
      text.insert(offset, s)
      modificationStamp++
    }

    override fun deleteString(startOffset: Int, endOffset: Int) {
      text.delete(startOffset, endOffset)
      modificationStamp++
    }

    override fun replaceString(startOffset: Int, endOffset: Int, s: CharSequence) {
      text.replace(startOffset, endOffset, s.toString())
      modificationStamp++
    }

    override fun isWritable(): Boolean = true

    override fun getModificationStamp(): Long = modificationStamp

    override fun createRangeMarker(
      startOffset: Int,
      endOffset: Int,
      surviveOnExternalChange: Boolean,
    ): RangeMarker = unsupported()

    override fun createGuardedBlock(startOffset: Int, endOffset: Int): RangeMarker = unsupported()

    override fun setText(text: CharSequence) {
      this.text.clear()
      this.text.append(text)
      modificationStamp++
    }

    private fun unsupported(): Nothing {
      throw UnsupportedOperationException("MutableTestDocument only supports text operations")
    }
  }
}
