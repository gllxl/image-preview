package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.extractor.CssImageReferenceExtractor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CssImageReferenceExtractorTest {

  @Test
  fun testUrlFunctionContentSupportsQuotedAndUnquotedPaths() {
    assertEquals("../assets/logo.png", CssImageReferenceExtractor.findUrlFunctionContent("url(../assets/logo.png)"))
    assertEquals("'../assets/logo.png'", CssImageReferenceExtractor.findUrlFunctionContent("url('../assets/logo.png')"))
    assertEquals("\"https://cdn.example.com/logo.svg\"", CssImageReferenceExtractor.findUrlFunctionContent("url(\"https://cdn.example.com/logo.svg\")"))
  }

  @Test
  fun testUrlFunctionContentIgnoresNonUrlText() {
    assertNull(CssImageReferenceExtractor.findUrlFunctionContent("../assets/logo.png"))
    assertNull(CssImageReferenceExtractor.findUrlFunctionContent("linear-gradient(red, blue)"))
    assertNull(CssImageReferenceExtractor.findUrlFunctionContent("url()"))
  }
}
