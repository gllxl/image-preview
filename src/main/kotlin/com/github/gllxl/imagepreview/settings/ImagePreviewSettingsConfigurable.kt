package com.github.gllxl.imagepreview.settings

import com.github.gllxl.imagepreview.service.imagePreviewService
import com.github.gllxl.imagepreview.service.RefreshScheduler
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.COLUMNS_TINY
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.rows

class ImagePreviewSettingsConfigurable : BoundConfigurable("Image Preview") {
  private val refreshScheduler = RefreshScheduler()

  override fun apply() {
    super.apply()
    refreshOpenProjects(clearCache = true)
  }

  override fun createPanel(): DialogPanel {
    val settings = ImagePreviewSettings.instance

    return panel {
      group("Preview") {
        row {
          checkBox("Show preview icon")
            .align(AlignX.LEFT)
            .gap(RightGap.SMALL)
            .resizableColumn()
            .comment("Display a gutter icon next to supported image references.")
            .bindSelected(settings::isShowPreviewIcon)
        }

        row {
          checkBox("Show thumbnail image")
            .align(AlignX.LEFT)
            .gap(RightGap.SMALL)
            .resizableColumn()
            .comment("Use the loaded image as the gutter icon. When disabled, the default image icon is used.")
            .bindSelected(settings::isShowActualImageIcon)
        }

        row {
          checkBox("Show inline image size")
            .align(AlignX.LEFT)
            .gap(RightGap.SMALL)
            .resizableColumn()
            .comment("Display image dimensions and file size at the end of the line after the image is loaded.")
            .bindSelected(settings::isShowInlineImageSize)
        }
      }

      group("Loading") {
        row {
          checkBox("Load remote images automatically")
            .align(AlignX.LEFT)
            .gap(RightGap.SMALL)
            .resizableColumn()
            .comment("Fetch HTTP and HTTPS images in the background when an image reference is detected.")
            .bindSelected(settings::isLoadRemoteImagesAutomatically)
        }

        row {
          checkBox("Load local images automatically")
            .align(AlignX.LEFT)
            .gap(RightGap.SMALL)
            .resizableColumn()
            .comment("Read local file images in the background when an image reference is detected.")
            .bindSelected(settings::isLoadLocalImagesAutomatically)
        }

        row {
          checkBox("Load only when Preview is clicked")
            .align(AlignX.LEFT)
            .gap(RightGap.SMALL)
            .resizableColumn()
            .comment("Disable background image loading. The Preview action can still load images on demand.")
            .bindSelected(settings::isLoadOnlyWhenPreviewIsClicked)
        }
      }

      group("Limits") {
        row("Maximum image size (MB):") {
          intTextField(ImagePreviewSettings.MINIMUM_IMAGE_SIZE_MB..ImagePreviewSettings.MAXIMUM_IMAGE_SIZE_MB)
            .columns(COLUMNS_TINY)
            .comment("Images larger than this limit are skipped before decoding.")
            .bindIntText(settings::maximumImageSizeMb)
        }

        row {
          checkBox("Scale popup image to fit screen")
            .align(AlignX.LEFT)
            .gap(RightGap.SMALL)
            .resizableColumn()
            .comment("Shrink large preview popups so the full image remains visible.")
            .bindSelected(settings::isScalePopupImageToFitScreen)
        }
      }

      group("Advanced") {
        row("Allowed domains:") {
          textArea()
            .rows(3)
            .columns(COLUMNS_LARGE)
            .align(AlignX.FILL)
            .resizableColumn()
            .comment("Blank allows all remote domains. Separate entries with commas or new lines; use *.example.com for subdomains.")
            .bindText(settings::allowedDomains)
        }

        row {
          button("Clear preview cache") {
            refreshOpenProjects(clearCache = true)
          }
        }
      }
    }
  }

  private fun refreshOpenProjects(clearCache: Boolean) {
    ProjectManager.getInstance().openProjects.forEach { project ->
      if (clearCache) {
        imagePreviewService(project).clearCache()
      }
      refreshScheduler.schedule(project)
    }
  }
}
