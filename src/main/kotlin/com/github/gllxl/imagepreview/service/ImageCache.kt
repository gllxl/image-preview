package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.model.ImageResource
import java.util.LinkedHashMap

class ImageCache(private val maxEntries: Int) {
  private val entries = object : LinkedHashMap<String, ImageResource>(maxEntries, 0.75f, true) {
    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageResource>?): Boolean {
      return size > maxEntries
    }
  }

  fun get(imageUrl: String): ImageResource? {
    return synchronized(entries) {
      entries[imageUrl]
    }
  }

  fun put(imageUrl: String, image: ImageResource) {
    synchronized(entries) {
      entries[imageUrl] = image
    }
  }

  fun clear() {
    synchronized(entries) {
      entries.clear()
    }
  }

  fun size(): Int {
    return synchronized(entries) {
      entries.size
    }
  }
}
