package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.dto.ImageDTO
import com.intellij.util.net.HttpConfigurable
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

    if (!isImage) {
      return null
    }

    if (hasImageInPools(imgUrl)) {
//      println("hasImageInPools -> $imgUrl")
      return pool[imgUrl]?.let { sizePool[imgUrl]?.let { it1 -> ImageDTO(it, it1) } }
    }

//    println("start fetch -> $imgUrl")

    val config = HttpConfigurable.getInstance()

    try {
      val res = config.openHttpConnection(imgUrl)

      if (res.responseCode != HttpURLConnection.HTTP_OK ) {
        return null
      }
      val image = ImageIO.read(res.inputStream)

      pool[imgUrl] = image
      sizePool[imgUrl] = readableFileSize(res.contentLength)

      return ImageDTO(image, readableFileSize(res.contentLength))

    } catch (e: Error) {
      return null
    }

  }

  fun getImageFromPool (imgUrl: String): BufferedImage? {
    if (hasImageInPools(imgUrl)) {
      return pool[imgUrl]
    }
    return null
  }
}