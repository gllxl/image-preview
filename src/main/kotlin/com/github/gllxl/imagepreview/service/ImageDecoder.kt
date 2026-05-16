package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.imageReferenceExtension
import com.github.gllxl.imagepreview.model.ImageResource
import com.github.gllxl.imagepreview.readableFileSize
import com.intellij.openapi.diagnostic.Logger
import org.apache.batik.transcoder.SVGAbstractTranscoder
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.ImageTranscoder
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.min
import kotlin.math.roundToInt
import org.w3c.dom.Document

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
    val document = parseSvgDocument(bytes)
    val documentSize = svgDocumentSize(document)
    val (width, height) = scaledSvgSize(documentSize)
    val image = renderSvg(document, width, height)

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

  private fun parseSvgDocument(bytes: ByteArray): Document {
    val factory = DocumentBuilderFactory.newInstance()
    factory.isNamespaceAware = true
    factory.isXIncludeAware = false
    factory.setExpandEntityReferences(false)
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
    factory.setFeatureIfSupported("http://xml.org/sax/features/external-general-entities", false)
    factory.setFeatureIfSupported("http://xml.org/sax/features/external-parameter-entities", false)
    factory.setFeatureIfSupported("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    factory.setAttributeIfSupported(ACCESS_EXTERNAL_DTD, "")
    factory.setAttributeIfSupported(ACCESS_EXTERNAL_SCHEMA, "")

    return ByteArrayInputStream(bytes).use {
      factory.newDocumentBuilder().parse(it)
    }
  }

  private fun svgDocumentSize(document: Document): Rectangle2D.Float {
    val root = document.documentElement
    val width = svgLength(root.getAttribute("width"))
    val height = svgLength(root.getAttribute("height"))
    val viewBoxSize = svgViewBoxSize(root.getAttribute("viewBox"))

    return Rectangle2D.Float(
      0f,
      0f,
      width ?: viewBoxSize?.first ?: maxSvgPreviewSide,
      height ?: viewBoxSize?.second ?: maxSvgPreviewSide,
    )
  }

  private fun svgLength(value: String?): Float? {
    if (value.isNullOrBlank()) {
      return null
    }

    val trimmed = value.trim()
    if (trimmed.endsWith("%")) {
      return null
    }

    return SVG_LENGTH_REGEX.find(trimmed)
      ?.groupValues
      ?.get(1)
      ?.toFloatOrNull()
      ?.takeIf { it > 0f }
  }

  private fun svgViewBoxSize(value: String?): Pair<Float, Float>? {
    if (value.isNullOrBlank()) {
      return null
    }

    val values = value
      .trim()
      .split(Regex("[,\\s]+"))
      .mapNotNull { it.toFloatOrNull() }

    if (values.size != 4 || values[2] <= 0f || values[3] <= 0f) {
      return null
    }

    return Pair(values[2], values[3])
  }

  private fun renderSvg(document: Document, width: Float, height: Float): BufferedImage {
    val transcoder = BufferedImageTranscoder()
    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, width)
    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, height)
    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD, false)
    transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_ALLOW_EXTERNAL_RESOURCES, false)
    transcoder.transcode(TranscoderInput(document), TranscoderOutput())
    return transcoder.image ?: error("SVG transcoder did not produce an image")
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
    private const val ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD"
    private const val ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema"
    private val SVG_LENGTH_REGEX = Regex("^([+-]?(?:\\d+(?:\\.\\d*)?|\\.\\d+))")
    private val logger = Logger.getInstance(ImageDecoder::class.java)
  }

  private class BufferedImageTranscoder : ImageTranscoder() {
    var image: BufferedImage? = null
      private set

    override fun createImage(width: Int, height: Int): BufferedImage {
      return BufferedImage(maxOf(1, width), maxOf(1, height), BufferedImage.TYPE_INT_ARGB)
    }

    override fun writeImage(image: BufferedImage, output: TranscoderOutput) {
      this.image = image
    }
  }
}

private fun DocumentBuilderFactory.setFeatureIfSupported(feature: String, value: Boolean) {
  runCatching {
    setFeature(feature, value)
  }
}

private fun DocumentBuilderFactory.setAttributeIfSupported(name: String, value: String) {
  runCatching {
    setAttribute(name, value)
  }
}
