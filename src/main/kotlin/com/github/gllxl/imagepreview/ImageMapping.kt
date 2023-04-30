package com.github.gllxl.imagepreview

import com.intellij.openapi.vfs.VirtualFile

object ImageMapping {

  private val pool = HashMap<VirtualFile, HashMap<Number, String>>()

  private fun hasFileInPools(file: VirtualFile): Boolean {
    return pool.containsKey(file)
  }

  fun setLineMapping(file: VirtualFile, lineNumber: Number, imgUrl: String) {
    println("setLineMapping$lineNumber")
    val inHashMap = HashMap<Number, String>()
    inHashMap[lineNumber] = imgUrl

    val target = pool[file]
    if (hasFileInPools(file)) {
      target?.put(lineNumber, imgUrl)
      pool[file]?.let { getKeyByValue(it, imgUrl) }?.forEach {
        if (it !== lineNumber) {
          pool[file]?.remove(it)
        }
      }
    } else {
      pool[file] = inHashMap
    }
  }

  fun getLineMapping(file: VirtualFile, lineNumber: Number): String? {
    return pool[file]?.get(lineNumber)
  }

}