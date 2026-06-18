package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.service.ImageDecoder
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ImageDecoderTest {

  private val decoder = ImageDecoder()

  @Test
  fun testSvgBytesAreRenderedToBufferedImage() {
    val svg = """
      <svg xmlns="http://www.w3.org/2000/svg" width="120" height="80" viewBox="0 0 120 80">
        <rect width="120" height="80" fill="#4f8cff"/>
        <circle cx="60" cy="40" r="24" fill="#ffffff"/>
      </svg>
    """.trimIndent().toByteArray()

    val image = decoder.decode(svg, "https://cdn.example.com/icon.svg")

    assertNotNull(image)
    assertEquals(120, image.previewImage.width)
    assertEquals(80, image.previewImage.height)
    assertEquals(120, image.originalWidth)
    assertEquals(80, image.originalHeight)
  }

  @Test
  fun testSvgViewBoxOnlyUsesViewBoxDimensions() {
    val svg = """
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 150 75">
        <rect width="150" height="75" fill="#4f8cff"/>
      </svg>
    """.trimIndent().toByteArray()

    val image = decoder.decode(svg, "https://cdn.example.com/viewbox.svg")

    assertNotNull(image)
    assertEquals(150, image.previewImage.width)
    assertEquals(75, image.previewImage.height)
    assertEquals(150, image.originalWidth)
    assertEquals(75, image.originalHeight)
  }

  @Test
  fun testSvgWithDoctypeIsRenderedWithoutLoadingExternalDtd() {
    val svg = """
      <?xml version="1.0" standalone="no"?>
      <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
      <svg xmlns="http://www.w3.org/2000/svg" width="24" height="16">
        <rect width="24" height="16" fill="#4f8cff"/>
      </svg>
    """.trimIndent().toByteArray()

    val image = decoder.decode(svg, "https://cdn.example.com/exported.svg")

    assertNotNull(image)
    assertEquals(24, image.previewImage.width)
    assertEquals(16, image.previewImage.height)
  }

  @Test
  fun testSvgDecodeIgnoresBrokenJaxpFactorySystemProperty() {
    val svg = """
      <svg xmlns="http://www.w3.org/2000/svg" width="18" height="12">
        <rect width="18" height="12" fill="#4f8cff"/>
      </svg>
    """.trimIndent().toByteArray()
    val propertyName = "javax.xml.parsers.DocumentBuilderFactory"
    val previousValue = System.getProperty(propertyName)

    System.setProperty(propertyName, "com.example.DoesNotExist")
    try {
      val image = decoder.decode(svg, "https://cdn.example.com/no-jaxp-provider.svg")

      assertNotNull(image)
      assertEquals(18, image.previewImage.width)
      assertEquals(12, image.previewImage.height)
    } finally {
      if (previousValue == null) {
        System.clearProperty(propertyName)
      } else {
        System.setProperty(propertyName, previousValue)
      }
    }
  }

  @Test
  fun testSvgContentTypeIsHonoredWhenUrlHasDifferentExtension() {
    val svg = """
      <svg xmlns="http://www.w3.org/2000/svg" width="40" height="30">
        <rect width="40" height="30" fill="red"/>
      </svg>
    """.trimIndent().toByteArray()

    val image = decoder.decode(svg, "https://cdn.example.com/image", "image/svg+xml; charset=utf-8")

    assertNotNull(image)
    assertEquals(40, image.previewImage.width)
    assertEquals(30, image.previewImage.height)
  }

  @Test
  fun testSvgFileUriIsRenderedToBufferedImage() {
    val svg = """
      <svg xmlns="http://www.w3.org/2000/svg" width="33" height="22">
        <rect width="33" height="22" fill="green"/>
      </svg>
    """.trimIndent().toByteArray()

    val image = decoder.decode(svg, "file:///tmp/local-icon.svg")

    assertNotNull(image)
    assertEquals(33, image.previewImage.width)
    assertEquals(22, image.previewImage.height)
  }

  @Test
  fun testLargeSvgPreviewIsScaledDownButKeepsOriginalDimensions() {
    val svg = """
      <svg xmlns="http://www.w3.org/2000/svg" width="5000" height="2500" viewBox="0 0 5000 2500">
        <rect width="5000" height="2500" fill="black"/>
      </svg>
    """.trimIndent().toByteArray()

    val image = decoder.decode(svg, "https://cdn.example.com/large.svg")

    assertNotNull(image)
    assertEquals(1024, image.previewImage.width)
    assertEquals(512, image.previewImage.height)
    assertEquals(5000, image.originalWidth)
    assertEquals(2500, image.originalHeight)
  }

  @Test
  fun testRasterBytesStillUseImageIo() {
    val original = BufferedImage(11, 7, BufferedImage.TYPE_INT_ARGB)
    val output = ByteArrayOutputStream()
    ImageIO.write(original, "png", output)

    val image = decoder.decode(output.toByteArray(), "https://cdn.example.com/image.png")

    assertNotNull(image)
    assertEquals(11, image.previewImage.width)
    assertEquals(7, image.previewImage.height)
    assertEquals(11, image.originalWidth)
    assertEquals(7, image.originalHeight)
  }

  @Test
  fun testInvalidRasterBytesReturnNull() {
    val image = decoder.decode("not an image".toByteArray(), "https://cdn.example.com/image.png")

    assertNull(image)
  }
}
