package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.service.LocalImageLoader
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import kotlin.io.path.writeText
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LocalImageLoaderTest {

  @Test
  fun testLoadsLocalImageFromFileUri() {
    val imagePath = Files.createTempFile("image-preview-local", ".png")
    writePng(imagePath, width = 13, height = 9)

    val image = LocalImageLoader().load(imagePath.toUri().toString())

    assertNotNull(image)
    assertEquals(imagePath.toUri().toString(), image.sourceUrl)
    assertEquals(13, image.previewImage.width)
    assertEquals(9, image.previewImage.height)
  }

  @Test
  fun testLoadsLocalImageFromFileUriWithQueryAndFragment() {
    val imagePath = Files.createTempFile("image-preview-local-query", ".png")
    writePng(imagePath, width = 17, height = 11)

    val image = LocalImageLoader().load("${imagePath.toUri()}?version=1#preview")

    assertNotNull(image)
    assertEquals(imagePath.toUri().toString(), image.sourceUrl)
    assertEquals(17, image.previewImage.width)
    assertEquals(11, image.previewImage.height)
  }

  @Test
  fun testLoadsLocalImageFromAbsolutePath() {
    val imagePath = Files.createTempFile("image-preview-absolute", ".png")
    writePng(imagePath, width = 7, height = 5)

    val image = LocalImageLoader().load(imagePath.toString())

    assertNotNull(image)
    assertEquals(imagePath.toUri().toString(), image.sourceUrl)
    assertEquals(7, image.previewImage.width)
    assertEquals(5, image.previewImage.height)
  }

  @Test
  fun testLoadsLocalSvgFromFileUri() {
    val imagePath = Files.createTempFile("image-preview-local-svg", ".svg")
    imagePath.writeText(
      """
        <svg xmlns="http://www.w3.org/2000/svg" width="19" height="13">
          <rect width="19" height="13" fill="#33aa66"/>
        </svg>
      """.trimIndent(),
    )

    val image = LocalImageLoader().load(imagePath.toUri().toString())

    assertNotNull(image)
    assertEquals(imagePath.toUri().toString(), image.sourceUrl)
    assertEquals(19, image.originalWidth)
    assertEquals(13, image.originalHeight)
    assertEquals(19, image.previewImage.width)
    assertEquals(13, image.previewImage.height)
  }

  @Test
  fun testMissingOrOversizedLocalImageReturnsNull() {
    val missing = Files.createTempDirectory("image-preview-loader").resolve("missing.png")
    assertNull(LocalImageLoader().load(missing.toUri().toString()))
    assertNull(LocalImageLoader().load("file:// bad uri /missing.png"))

    val oversized = Files.createTempFile("image-preview-large", ".png")
    Files.write(oversized, ByteArray(32) { 1 })

    assertNull(LocalImageLoader(maxImageBytes = 16).load(oversized.toUri().toString()))
  }

  @Test
  fun testLocalImageSizeLimitCanComeFromSettingsProvider() {
    val oversized = Files.createTempFile("image-preview-provider-large", ".png")
    Files.write(oversized, ByteArray(32) { 1 })

    assertNull(LocalImageLoader(maxImageBytesProvider = { 16 }).load(oversized.toUri().toString()))
  }

  private fun writePng(path: java.nio.file.Path, width: Int, height: Int) {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val output = ByteArrayOutputStream()
    ImageIO.write(image, "png", output)
    Files.write(path, output.toByteArray())
  }
}
