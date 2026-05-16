package com.github.gllxl.imagepreview.model

import com.intellij.openapi.vfs.VirtualFile

data class ImageReference(
  val file: VirtualFile,
  val lineNumber: Int,
  val imageUrl: String,
)
