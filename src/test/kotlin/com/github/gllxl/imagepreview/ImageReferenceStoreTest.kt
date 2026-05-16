package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.service.ImageReferenceStore
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
  fun testClearRemovesAllMappings() {
    val store = ImageReferenceStore()
    val file = LightVirtualFile("index.css")
    val imageUrl = "https://cdn.example.com/logo.png"

    store.setLineMapping(file, 1, imageUrl)
    store.clear()

    assertNull(store.getLineMapping(file, 1))
  }
}
