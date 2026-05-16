package com.github.gllxl.imagepreview.model

sealed class ImageLoadState {
  data object NotRequested : ImageLoadState()
  data object Loading : ImageLoadState()
  data class Ready(val image: ImageResource) : ImageLoadState()
  data class Failed(val failedAt: Long) : ImageLoadState()
}
