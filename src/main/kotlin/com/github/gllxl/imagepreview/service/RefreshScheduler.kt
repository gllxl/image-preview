package com.github.gllxl.imagepreview.service

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project

class RefreshScheduler {
  fun schedule(project: Project?) {
    if (project?.isDisposed != false) {
      return
    }

    DaemonCodeAnalyzer.getInstance(project).restart(RefreshScheduler::class.java)
    FileEditorManager.getInstance(project).selectedTextEditor?.component?.repaint()
  }
}
