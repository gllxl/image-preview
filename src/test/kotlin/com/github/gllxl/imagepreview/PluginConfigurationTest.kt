package com.github.gllxl.imagepreview

import java.nio.file.Files
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PluginConfigurationTest {

  @Test
  fun testSettingsPageAndServiceAreRegistered() {
    val document = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder()
      .parse(pluginXmlPath().toFile())

    assertEquals("Image Preview", document.getElementsByTagName("name").item(0).textContent)

    val configurable = document.getElementsByTagName("applicationConfigurable")
    assertEquals(1, configurable.length)
    assertEquals("tools", configurable.item(0).attributes.getNamedItem("parentId").nodeValue)
    assertEquals(
      "com.github.gllxl.imagepreview.settings.ImagePreviewSettingsConfigurable",
      configurable.item(0).attributes.getNamedItem("instance").nodeValue,
    )
    assertEquals("Image Preview", configurable.item(0).attributes.getNamedItem("displayName").nodeValue)

    val applicationServices = document.getElementsByTagName("applicationService")
    val serviceImplementations = (0 until applicationServices.length).map {
      applicationServices.item(it).attributes.getNamedItem("serviceImplementation").nodeValue
    }

    assertTrue(
      "com.github.gllxl.imagepreview.settings.ImagePreviewSettings" in serviceImplementations,
      "ImagePreviewSettings must be registered as an application service",
    )

    val dependencies = document.getElementsByTagName("depends")
    val dependencyIds = (0 until dependencies.length).map { dependencies.item(it).textContent.trim() }
    assertTrue("com.intellij.modules.json" in dependencyIds, "JSON plugin dependency must be declared")
    assertTrue("JavaScript" in dependencyIds, "JavaScript plugin dependency must be declared")
    assertTrue("com.intellij.css" in dependencyIds, "CSS plugin dependency must be declared")

    val contributors = document.getElementsByTagName("runLineMarkerContributor")
    val contributorLanguages = (0 until contributors.length).map {
      contributors.item(it).attributes.getNamedItem("language").nodeValue
    }

    assertTrue("JavaScript" in contributorLanguages, "JavaScript line marker must be registered")
    assertTrue("TypeScript" in contributorLanguages, "TypeScript line marker must be registered")
    assertTrue("JSON" in contributorLanguages, "JSON line marker must be registered")
    assertTrue("CSS" in contributorLanguages, "CSS line marker must be registered")
  }

  private fun pluginXmlPath(): Path {
    val path = Path.of("src/main/resources/META-INF/plugin.xml")
    assertTrue(Files.isRegularFile(path), "plugin.xml should exist at $path")
    return path
  }
}
