package com.github.gllxl.imagepreview.settings

import java.net.IDN
import java.net.URI

object ImagePreviewDomainPolicy {
  fun isAllowed(imageUrl: String, allowedDomains: String): Boolean {
    val allowedPatterns = parseAllowedDomains(allowedDomains)
    if (allowedPatterns.isEmpty()) {
      return true
    }

    val host = hostFromUrl(imageUrl) ?: return false
    return allowedPatterns.any { it.matches(host) }
  }

  internal fun parseAllowedDomains(allowedDomains: String): List<DomainPattern> {
    return allowedDomains
      .split(Regex("[,;\\s]+"))
      .mapNotNull(::domainPattern)
      .distinct()
  }

  private fun domainPattern(rawPattern: String): DomainPattern? {
    val trimmed = rawPattern.trim().trimEnd('.', '/')
    if (trimmed.isBlank()) {
      return null
    }
    if (trimmed == "*") {
      return DomainPattern("*", includeSubdomains = true)
    }

    val includeSubdomains = trimmed.startsWith("*.")
    val domain = normalizeHost(trimmed.removePrefix("*."))
      ?: return null
    return DomainPattern(domain, includeSubdomains)
  }

  private fun hostFromUrl(imageUrl: String): String? {
    return runCatching {
      URI(imageUrl.trim()).host?.let(::normalizeHost)
    }.getOrNull()
  }

  private fun normalizeHost(rawHost: String): String? {
    val hostCandidate = runCatching {
      val value = if (rawHost.contains("://")) rawHost else "https://$rawHost"
      URI(value).host
    }.getOrNull()
      ?: rawHost.substringBefore('/').substringBefore(':')

    val normalized = hostCandidate
      .trim()
      .trimEnd('.')
      .lowercase()
      .takeIf { it.isNotBlank() }
      ?: return null

    return runCatching { IDN.toASCII(normalized) }.getOrDefault(normalized)
  }

  internal data class DomainPattern(
    val domain: String,
    val includeSubdomains: Boolean,
  ) {
    fun matches(host: String): Boolean {
      if (domain == "*") {
        return true
      }

      if (host == domain) {
        return true
      }

      return includeSubdomains && host.endsWith(".$domain")
    }
  }
}
