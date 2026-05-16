package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.imageReferenceExtension
import com.github.gllxl.imagepreview.model.ImageResource
import com.github.gllxl.imagepreview.readableFileSize
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.svg.getSvgDocumentSize
import com.intellij.ui.svg.renderSvgWithSize
import java.awt.geom.Rectangle2D
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.min
import kotlin.math.roundToInt

class ImageDecoder(
  private val maxSvgPreviewSide: Float = MAX_SVG_PREVIEW_SIDE,
) {
  fun decode(
    bytes: ByteArray,
    sourceUrl: String,
    contentType: String? = null,
    byteSize: Long = bytes.size.toLong(),
  ): ImageResource? {
    return try {
      if (isSvgImage(sourceUrl, contentType)) {
        decodeSvg(bytes, sourceUrl, contentType, byteSize)
      } else {
        decodeRaster(bytes, sourceUrl, contentType, byteSize)
      }
    } catch (e: Exception) {
      logger.warn("Failed to decode image preview: $sourceUrl", e)
      null
    }
  }

  private fun decodeRaster(
    bytes: ByteArray,
    sourceUrl: String,
    contentType: String?,
    byteSize: Long,
  ): ImageResource? {
    val image = ImageIO.read(ByteArrayInputStream(bytes)) ?: return null
    return ImageResource(
      sourceUrl = sourceUrl,
      previewImage = image,
      imageSize = readableFileSize(byteSize),
      byteSize = byteSize,
      contentType = contentType,
    )
  }

  private fun decodeSvg(
    bytes: ByteArray,
    sourceUrl: String,
    contentType: String?,
    byteSize: Long,
  ): ImageResource? {
    val documentSize = getSvgDocumentSize(bytes)
    val (width, height) = scaledSvgSize(documentSize)
    val image = ByteArrayInputStream(bytes).use {
      renderSvgWithSize(it, width, height, 1f)
    }

    return ImageResource(
      sourceUrl = sourceUrl,
      previewImage = image,
      imageSize = readableFileSize(byteSize),
      byteSize = byteSize,
      contentType = contentType,
      originalWidth = positiveSize(documentSize.width, image.width),
      originalHeight = positiveSize(documentSize.height, image.height),
    )
  }

  private fun isSvgImage(sourceUrl: String, contentType: String?): Boolean {
    return imageReferenceExtension(sourceUrl) == "svg" ||
      contentType?.substringBefore(';')?.trim()?.equals("image/svg+xml", true) == true
  }

  private fun scaledSvgSize(size: Rectangle2D.Float): Pair<Float, Float> {
    val width = size.width.takeIf { it > 0f } ?: maxSvgPreviewSide
    val height = size.height.takeIf { it > 0f } ?: maxSvgPreviewSide
    val scale = min(1f, maxSvgPreviewSide / maxOf(width, height))
    return Pair(width * scale, height * scale)
  }

  private fun positiveSize(value: Float, fallback: Int): Int {
    if (value <= 0f) {
      return fallback
    }
    return maxOf(1, value.roundToInt())
  }

  companion object {
    const val MAX_SVG_PREVIEW_SIDE = 1024f
    private val logger = Logger.getInstance(ImageDecoder::class.java)
  }
}
