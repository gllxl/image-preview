package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.isImageReference
import com.intellij.openapi.vfs.VirtualFile
import java.util.WeakHashMap

class ImageReferenceStore {
  private val references = WeakHashMap<VirtualFile, MutableMap<Int, String>>()

  fun setLineMapping(file: VirtualFile, lineNumber: Int, imageUrl: String) {
    synchronized(references) {
      if (!isImageReference(imageUrl)) {
        references[file]?.remove(lineNumber)
        return
      }

      references.getOrPut(file) { HashMap() }[lineNumber] = imageUrl
    }
  }

  fun getLineMapping(file: VirtualFile, lineNumber: Int): String? {
    return synchronized(references) {
      references[file]?.get(lineNumber)
    }
  }

  fun clear() {
    synchronized(references) {
      references.clear()
    }
  }
}
