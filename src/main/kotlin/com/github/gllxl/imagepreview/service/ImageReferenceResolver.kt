package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.isImageReference
import com.github.gllxl.imagepreview.isImageUrl
import com.github.gllxl.imagepreview.removeUrlQuotes
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.net.URI
import java.nio.file.InvalidPathException
import java.nio.file.Path

class ImageReferenceResolver(private val project: Project? = null) {
  fun resolve(rawReference: String, containingFile: VirtualFile?): String? {
    return resolve(
      rawReference = rawReference,
      sourceDirectory = sourceDirectory(containingFile),
      projectBaseDirectory = project?.basePath?.let(::safePath),
    )
  }

  internal fun resolve(
    rawReference: String,
    sourceDirectory: Path?,
    projectBaseDirectory: Path? = null,
  ): String? {
    val reference = removeUrlQuotes(rawReference)
    if (isImageUrl(reference)) {
      return reference
    }

    if (!isImageReference(reference)) {
      return null
    }

    val path = localPath(reference, sourceDirectory, projectBaseDirectory) ?: return null
    return path.toAbsolutePath().normalize().toUri().toString()
  }

  private fun sourceDirectory(containingFile: VirtualFile?): Path? {
    if (containingFile == null || !containingFile.isInLocalFileSystem) {
      return null
    }

    return if (containingFile.isDirectory) {
      safePath(containingFile.path)
    } else {
      containingFile.parent?.path?.let(::safePath)
    }
  }

  private fun localPath(reference: String, sourceDirectory: Path?, projectBaseDirectory: Path?): Path? {
    if (reference.startsWith("file:", true)) {
      return fileUriPath(reference)
    }

    val rawPath = reference
      .substringBefore('#')
      .substringBefore('?')
      .trim()

    val path = safePath(rawPath) ?: return null
    if (path.isAbsolute) {
      return path.normalize()
    }

    val baseDirectory = sourceDirectory ?: projectBaseDirectory ?: return null
    return baseDirectory.resolve(path).normalize()
  }

  private fun safePath(path: String): Path? {
    return try {
      Path.of(path)
    } catch (e: InvalidPathException) {
      null
    }
  }

  private fun fileUriPath(reference: String): Path? {
    return try {
      val uri = URI(reference)
      val pathOnlyUri = URI(uri.scheme, uri.authority, uri.path, null, null)
      Path.of(pathOnlyUri).normalize()
    } catch (e: Exception) {
      null
    }
  }
}
