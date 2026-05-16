package com.github.gllxl.imagepreview

import com.github.gllxl.imagepreview.settings.ImagePreviewDomainPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImagePreviewDomainPolicyTest {

  @Test
  fun testBlankAllowedDomainsAllowsEveryRemoteImage() {
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://cdn.example.com/image.png", ""))
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://assets.example.net/image.png", "  \n "))
  }

  @Test
  fun testExactDomainAllowsOnlyThatHost() {
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://cdn.example.com/image.png", "cdn.example.com"))
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://CDN.EXAMPLE.COM/image.png", "CDN.EXAMPLE.COM"))
    assertFalse(ImagePreviewDomainPolicy.isAllowed("https://static.example.com/image.png", "cdn.example.com"))
    assertFalse(ImagePreviewDomainPolicy.isAllowed("https://sub.cdn.example.com/image.png", "cdn.example.com"))
  }

  @Test
  fun testWildcardDomainAllowsRootAndSubdomains() {
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://example.com/image.png", "*.example.com"))
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://cdn.example.com/image.png", "*.example.com"))
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://deep.cdn.example.com/image.png", "*.example.com"))
    assertFalse(ImagePreviewDomainPolicy.isAllowed("https://example.net/image.png", "*.example.com"))
  }

  @Test
  fun testAllowedDomainsCanBeSeparatedByCommasNewLinesOrSpaces() {
    val allowedDomains = """
      cdn.example.com,
      images.example.net assets.example.org
    """.trimIndent()

    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://cdn.example.com/image.png", allowedDomains))
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://images.example.net/image.png", allowedDomains))
    assertTrue(ImagePreviewDomainPolicy.isAllowed("https://assets.example.org/image.png", allowedDomains))
    assertFalse(ImagePreviewDomainPolicy.isAllowed("https://blocked.example.org/image.png", allowedDomains))
  }

  @Test
  fun testDomainParserAcceptsSchemeAndPathFromPastedUrls() {
    assertTrue(
      ImagePreviewDomainPolicy.isAllowed(
        "https://cdn.example.com/image.png",
        "https://cdn.example.com/assets/",
      ),
    )
  }

  @Test
  fun testInvalidRemoteUrlIsDeniedWhenAllowListIsConfigured() {
    assertFalse(ImagePreviewDomainPolicy.isAllowed("https:// invalid /image.png", "cdn.example.com"))
  }

  @Test
  fun testParseAllowedDomainsDeduplicatesEntries() {
    val domains = ImagePreviewDomainPolicy.parseAllowedDomains("cdn.example.com cdn.example.com")

    assertEquals(1, domains.size)
  }
}
