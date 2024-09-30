package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.dto.ImageDTO
import com.intellij.util.net.HttpConnectionUtils.openHttpConnection
import java.awt.image.BufferedImage
import java.net.HttpURLConnection
import javax.imageio.ImageIO

object ImagePool {

  private val pool = java.util.HashMap<String, BufferedImage>()
  private val sizePool = java.util.HashMap<String, String>()

  private fun hasImageInPools(url: String): Boolean {
//    println("hasImageInPools -> " + pool.containsKey(url))
    return pool.containsKey(url)
  }

  fun getImageByUrl (imgUrl: String): ImageDTO? {
//    println("getImageByUrl$imgUrl")

    val isImage = isImageUrl(imgUrl)
    val isSvg = isSvgUrl(imgUrl)

    if (!isImage && !isSvg) {
      return null
    }

    if (hasImageInPools(imgUrl)) {
      return pool[imgUrl]?.let { sizePool[imgUrl]?.let { it1 -> ImageDTO(it, it1) } }
    }

    if (isSvg) {
      val svg = loadSVG(imgUrl)
      return svg?.let { ImageDTO(it, "100") }
    } else {
      try {
        val res = openHttpConnection(imgUrl)

        if (res.responseCode != HttpURLConnection.HTTP_OK ) {
          return null
        }
        val image = ImageIO.read(res.url) ?: return null

        pool[imgUrl] = image
        sizePool[imgUrl] = readableFileSize(res.contentLength)

        return ImageDTO(image, readableFileSize(res.contentLength))

      } catch (e: Error) {
        return null
      }
    }
    return null
  }

  fun getImageFromPool (imgUrl: String): BufferedImage? {
    if (hasImageInPools(imgUrl)) {
      return pool[imgUrl]
    }
    return null
  }
}