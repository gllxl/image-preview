package com.github.gllxl.imagepreview

import com.intellij.util.net.HttpConfigurable
import java.awt.image.BufferedImage
import java.net.HttpURLConnection
import javax.imageio.ImageIO

object ImagePool {

  private val pool = java.util.HashMap<String, BufferedImage>()

  private fun hasImageInPools(url: String): Boolean {
    println("hasImageInPools -> " + pool.containsKey(url))
    return pool.containsKey(url)
  }

  fun getImageByUrl (imgUrl: String): BufferedImage? {
    println("getImageByUrl$imgUrl")

    val isImage = isImageUrl(imgUrl);

    if (!isImage) {
      return null
    }

    if (hasImageInPools(imgUrl)) {
      println("hasImageInPools -> $imgUrl")
      return pool[imgUrl]
    }

    println("start fetch -> $imgUrl")

    val config = HttpConfigurable.getInstance()
    val res = config.openHttpConnection(imgUrl)

    val image = ImageIO.read(res.inputStream)

    if (res.responseCode != HttpURLConnection.HTTP_OK) {
      return null
    }
    pool[imgUrl] = image

    return image
  }
}