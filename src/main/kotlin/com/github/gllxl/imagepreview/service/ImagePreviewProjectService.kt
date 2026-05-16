package com.github.gllxl.imagepreview.service

import com.github.gllxl.imagepreview.model.ImageResource
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ImagePreviewProjectService(private val project: Project) : Disposable {
  val references = ImageReferenceStore()
  val repository = ImageRepository()
  private val resolver = ImageReferenceResolver(project)

  fun getImage(imageUrl: String): ImageResource? {
    return repository.getImage(imageUrl)
  }

  fun requestImage(
    imageUrl: String,
    requestKind: ImageLoadRequestKind = ImageLoadRequestKind.AUTOMATIC,
    onLoaded: ((ImageResource) -> Unit)? = null,
  ) {
    repository.requestImage(imageUrl, project, requestKind, onLoaded)
  }

  fun resolveImageReference(rawReference: String, containingFile: VirtualFile?): String? {
    return resolver.resolve(rawReference, containingFile)
  }

  fun clearCache() {
    repository.clear()
  }

  override fun dispose() {
    references.clear()
    repository.clear()
  }
}

fun imagePreviewService(project: Project): ImagePreviewProjectService {
  return project.service()
}
