package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.service.ImageReferenceResolver
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageReferenceResolverTest {

  private val resolver = ImageReferenceResolver()

  @Test
  fun testRemoteImageUrlIsReturnedAsIs() {
    val imageUrl = "https://cdn.example.com/assets/logo.png?version=1#hero"

    assertEquals(imageUrl, resolver.resolve(imageUrl, sourceDirectory = null))
  }

  @Test
  fun testRelativePathResolvesAgainstSourceDirectory() {
    val root = Files.createTempDirectory("image-preview-resolver")
    val sourceDirectory = root.resolve("src/components").createDirectories()
    val image = root.resolve("src/components/assets/logo.png").also {
      it.parent.createDirectories()
      it.writeText("fake")
    }

    assertEquals(
      image.toUri().toString(),
      resolver.resolve("./assets/logo.png", sourceDirectory = sourceDirectory),
    )
  }

  @Test
  fun testParentRelativePathResolvesAgainstSourceDirectory() {
    val root = Files.createTempDirectory("image-preview-resolver")
    val sourceDirectory = root.resolve("src/components").createDirectories()
    val image = root.resolve("src/shared/icon.svg").also {
      it.parent.createDirectories()
      it.writeText("<svg/>")
    }

    assertEquals(
      image.toUri().toString(),
      resolver.resolve("../shared/icon.svg", sourceDirectory = sourceDirectory),
    )
  }

  @Test
  fun testAbsolutePathAndFileUriResolveToCanonicalFileUri() {
    val image = Files.createTempFile("image-preview-local", ".png")

    assertEquals(image.toUri().toString(), resolver.resolve(image.toString(), sourceDirectory = null))
    assertEquals(image.toUri().toString(), resolver.resolve(image.toUri().toString(), sourceDirectory = null))
  }

  @Test
  fun testProjectBaseIsUsedWhenSourceDirectoryIsUnavailable() {
    val root = Files.createTempDirectory("image-preview-project-base")
    val image = root.resolve("assets/logo.webp").also {
      it.parent.createDirectories()
      it.writeText("fake")
    }

    assertEquals(
      image.toUri().toString(),
      resolver.resolve("assets/logo.webp", sourceDirectory = null, projectBaseDirectory = root),
    )
  }

  @Test
  fun testMissingSupportedLocalPathStillResolvesWithoutTouchingDisk() {
    val root = Files.createTempDirectory("image-preview-missing")
    val missing = root.resolve("assets/missing.png")

    assertEquals(missing.toUri().toString(), resolver.resolve("assets/missing.png", sourceDirectory = root))
  }

  @Test
  fun testUnsupportedLocalPathIsIgnored() {
    val root = Files.createTempDirectory("image-preview-unsupported")

    assertNull(resolver.resolve("assets/readme.txt", sourceDirectory = root))
    assertNull(resolver.resolve("//cdn.example.com/logo.png", sourceDirectory = root))
    assertNull(resolver.resolve("file:// bad uri /logo.png", sourceDirectory = root))
  }

  @Test
  fun testFileUriQueryAndFragmentAreIgnoredForPathResolution() {
    val image = Files.createTempFile("image-preview-local-query", ".svg")

    assertEquals(
      image.toUri().toString(),
      resolver.resolve("${image.toUri()}?version=1#icon", sourceDirectory = null),
    )
  }
}
