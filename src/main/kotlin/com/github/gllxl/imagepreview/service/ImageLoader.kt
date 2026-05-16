package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.isImageUrl
import com.github.gllxl.imagepreview.model.ImageResource
import com.github.gllxl.imagepreview.readableFileSize
import com.github.gllxl.imagepreview.settings.ImagePreviewSettings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.HttpRequests
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.HttpURLConnection
import java.nio.file.Files
import java.nio.file.Path

fun interface ImageLoader {
  @Throws(IOException::class)
  fun load(imageUrl: String): ImageResource?
}

class RoutingImageLoader(
  private val httpImageLoader: ImageLoader = HttpImageLoader(),
  private val localImageLoader: ImageLoader = LocalImageLoader(),
) : ImageLoader {
  override fun load(imageUrl: String): ImageResource? {
    return if (isImageUrl(imageUrl)) {
      httpImageLoader.load(imageUrl)
    } else {
      localImageLoader.load(imageUrl)
    }
  }
}

class HttpImageLoader(
  private val decoder: ImageDecoder = ImageDecoder(),
  private val maxImageBytes: Long? = null,
  private val maxImageBytesProvider: () -> Long = { ImagePreviewSettings.currentState().maximumImageBytes },
) : ImageLoader {
  override fun load(imageUrl: String): ImageResource? {
    val maxBytes = effectiveMaxImageBytes()

    return HttpRequests.request(imageUrl)
      .connectTimeout(CONNECT_TIMEOUT_MS)
      .readTimeout(READ_TIMEOUT_MS)
      .throwStatusCodeException(false)
      .connect { request ->
        val connection = request.connection
        if (connection is HttpURLConnection && connection.responseCode != HttpURLConnection.HTTP_OK) {
          return@connect null
        }

        val contentLength = connection.contentLengthLong
        if (contentLength > maxBytes) {
          logger.warn("Skip image preview because it is too large: $imageUrl (${readableFileSize(contentLength)})")
          return@connect null
        }

        val bytes = request.inputStream.use { readBytesLimited(it, maxBytes) }
        val byteSize = contentLength.takeIf { it > 0 } ?: bytes.size.toLong()
        decoder.decode(bytes, imageUrl, connection.contentType, byteSize)
      }
  }

  private fun effectiveMaxImageBytes(): Long {
    return (maxImageBytes ?: maxImageBytesProvider()).coerceAtLeast(1L)
  }

  private fun readBytesLimited(inputStream: InputStream, maxBytes: Long): ByteArray {
    val buffer = ByteArray(8 * 1024)
    val output = ByteArrayOutputStream()
    var totalBytes = 0L

    while (true) {
      val read = inputStream.read(buffer)
      if (read == -1) {
        return output.toByteArray()
      }

      totalBytes += read
      if (totalBytes > maxBytes) {
        throw IOException("Image preview response exceeds ${readableFileSize(maxBytes)}")
      }
      output.write(buffer, 0, read)
    }
  }

  companion object {
    const val CONNECT_TIMEOUT_MS = 5_000
    const val READ_TIMEOUT_MS = 10_000
    const val MAX_IMAGE_BYTES = 10L * 1024L * 1024L
    private val logger = Logger.getInstance(HttpImageLoader::class.java)
  }
}

class LocalImageLoader(
  private val decoder: ImageDecoder = ImageDecoder(),
  private val maxImageBytes: Long? = null,
  private val maxImageBytesProvider: () -> Long = { ImagePreviewSettings.currentState().maximumImageBytes },
) : ImageLoader {
  override fun load(imageUrl: String): ImageResource? {
    val maxBytes = effectiveMaxImageBytes()
    val path = localPath(imageUrl) ?: return null
    if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
      return null
    }

    val byteSize = Files.size(path)
    if (byteSize > maxBytes) {
      logger.warn("Skip local image preview because it is too large: $imageUrl (${readableFileSize(byteSize)})")
      return null
    }

    val bytes = Files.newInputStream(path).use { readBytesLimited(it, maxBytes) }
    val sourceUrl = path.toAbsolutePath().normalize().toUri().toString()
    return decoder.decode(bytes, sourceUrl, Files.probeContentType(path), byteSize)
  }

  private fun effectiveMaxImageBytes(): Long {
    return (maxImageBytes ?: maxImageBytesProvider()).coerceAtLeast(1L)
  }

  private fun localPath(imageUrl: String): Path? {
    return try {
      if (imageUrl.startsWith("file:", true)) {
        val uri = URI(imageUrl)
        Path.of(URI(uri.scheme, uri.authority, uri.path, null, null)).normalize()
      } else {
        Path.of(imageUrl).normalize()
      }
    } catch (e: Exception) {
      null
    }
  }

  private fun readBytesLimited(inputStream: InputStream, maxBytes: Long): ByteArray {
    val buffer = ByteArray(8 * 1024)
    val output = ByteArrayOutputStream()
    var totalBytes = 0L

    while (true) {
      val read = inputStream.read(buffer)
      if (read == -1) {
        return output.toByteArray()
      }

      totalBytes += read
      if (totalBytes > maxBytes) {
        throw IOException("Image preview response exceeds ${readableFileSize(maxBytes)}")
      }
      output.write(buffer, 0, read)
    }
  }

  companion object {
    const val MAX_IMAGE_BYTES = HttpImageLoader.MAX_IMAGE_BYTES
    private val logger = Logger.getInstance(LocalImageLoader::class.java)
  }
}
