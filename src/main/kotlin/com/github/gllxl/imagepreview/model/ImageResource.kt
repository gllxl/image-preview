package com.github.gllxl.imagepreview.model

import java.awt.image.BufferedImage

data class ImageResource(
  val sourceUrl: String,
  val previewImage: BufferedImage,
  val imageSize: String,
  val byteSize: Long,
  val contentType: String? = null,
  val originalWidth: Int = previewImage.width,
  val originalHeight: Int = previewImage.height,
) {
  val imageBuffered: BufferedImage
    get() = previewImage
}
