package com.github.gllxl.imagepreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UtilsTest {

    @Test
    fun testIsImageUrlAcceptsHttpImages() {
        val urls = listOf(
            "https://g.alicdn.com/buc-fe/sso-civil/1.19.0/assets/931f81d3d9d65243c35f0a481bd2872a.png",
            "https://img.zcool.cn/community/0147cb5f58366211013e3187e8c713.jpg",
            "http://g.alicdn.com/buc-fe/sso-civil/1.19.0/assets/931f81d3d9d65243c35f0a481bd2872a.png",
            "https://cdn.example.com/assets/logo.PNG?version=1#hero",
            "https://cdn.example.com/assets/icon.svg",
            "https://cdn.example.com/assets/animation.webp",
            " https://cdn.example.com/assets/photo.jpeg ",
        )

        urls.forEach {
            assertTrue(isImageUrl(it), "$it should be recognized as an image URL")
        }
    }

    @Test
    fun testImageUrlExtension() {
        assertEquals("svg", imageUrlExtension("https://cdn.example.com/assets/icon.svg?version=1#hero"))
        assertEquals("png", imageUrlExtension(" https://cdn.example.com/assets/logo.PNG "))
        assertEquals("gz", imageUrlExtension("https://cdn.example.com/assets/archive.tar.gz"))
        assertEquals(null, imageUrlExtension("file:///tmp/icon.svg"))
        assertEquals(null, imageUrlExtension("https://cdn.example.com/assets/logo"))
    }

    @Test
    fun testIsImageReferenceAcceptsRemoteAndLocalImages() {
        val references = listOf(
            "https://cdn.example.com/assets/logo.png?version=1#hero",
            "file:///tmp/icon.svg",
            "/tmp/photo.jpeg",
            "./assets/logo.webp",
            "../shared/banner.PNG",
        )

        references.forEach {
            assertTrue(isImageReference(it), "$it should be recognized as an image reference")
        }
    }

    @Test
    fun testIsImageUrlRejectsUnsupportedOrUnsafeUrls() {
        val urls = listOf(
            "//g.alicdn.com/buc-fe/sso-civil/1.19.0/assets/931f81d3d9d65243c35f0a481bd2872a.png",
            "ftp://cdn.example.com/assets/logo.png",
            "file:///tmp/logo.png",
            "https://cdn.example.com/assets/not-an-imagejpg",
            "https://cdn.example.com/assets/logo",
            "https://cdn.example.com/assets/archive.zip",
            "not a url",
            "",
        )

        urls.forEach {
            assertFalse(isImageUrl(it), "$it should not be recognized as an image URL")
        }
    }

    @Test
    fun testImageReferenceRejectsUnsupportedSchemesAndPaths() {
        val references = listOf(
            "//cdn.example.com/assets/logo.png",
            "ftp://cdn.example.com/assets/logo.png",
            "mailto:logo.png",
            "assets/not-an-imagejpg",
            "assets/archive.zip",
            "",
        )

        references.forEach {
            assertFalse(isImageReference(it), "$it should not be recognized as an image reference")
        }
    }

    @Test
    fun testRemoveUrlQuotes() {
        assertEquals("https://example.com/image.png", removeUrlQuotes(" 'https://example.com/image.png' "))
        assertEquals("https://example.com/image.png", removeUrlQuotes("`https://example.com/image.png`"))
        assertEquals("https://example.com/image.png", removeUrlQuotes("\"https://example.com/image.png\""))
        assertEquals("https://example.com/image.png", removeUrlQuotes("https://example.com/image.png"))
    }

    @Test
    fun testReadableFileSize() {
        assertEquals("0", readableFileSize(0))
        assertEquals("0", readableFileSize(-1))
        assertEquals("1 B", readableFileSize(1))
        assertEquals("1 kB", readableFileSize(1024))
        assertEquals("1.5 kB", readableFileSize(1536))
        assertEquals("1 MB", readableFileSize(1024 * 1024))
        assertEquals("1 GB", readableFileSize(1024L * 1024L * 1024L))
    }
}
