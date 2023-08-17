package com.intellij.plugin.copyOptionPath.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.options.ex.SingleConfigurableEditor
import com.intellij.openapi.options.newEditor.SettingsDialog
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.plugin.copyOptionPath.*
import com.intellij.ui.treeStructure.Tree
import com.intellij.ui.treeStructure.treetable.TreeTable
import com.intellij.util.ui.TextTransferable
import java.awt.Component
import javax.swing.AbstractButton
import javax.swing.JLabel

class CopyOptionsPath : DumbAwareAction() {

  init {
    isEnabledInModalContext = true
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = e.getData(PlatformDataKeys.CONTEXT_COMPONENT) != null
  }

  override fun actionPerformed(e: AnActionEvent) {
    val src = e.getData(PlatformDataKeys.CONTEXT_COMPONENT) ?: return
    val path = StringBuilder()
    if (!buildOptionPath(src, path, e)) return

    val result = trimFinalResult(path)
    LOG.debug("Selected path: $result")
    e.inputEvent.consume()
    CopyPasteManager.getInstance().setContents(TextTransferable(result, result))
  }

  private fun buildOptionPath(src: Component, path: StringBuilder, e: AnActionEvent): Boolean {
    val dialog = DialogWrapper.findInstance(src) ?: return false
    if (dialog is SettingsDialog) {
      val startPath = getPathFromSettingsDialog(dialog)
      appendItem(path, startPath)
    } else {
      appendItem(path, dialog.title)
    if (dialog is SingleConfigurableEditor
        && (ApplicationNamesInfo.getInstance().productName.equals("idea", true)
                || ApplicationNamesInfo.getInstance().productName.contains("edu", true)
                )
    ) {
        appendPathFromProjectStructureDialog(dialog.configurable, path)
      }
    }

    getMiddlePath(src, path)

    if (src is TreeTable) {
      val selectedRow = if (src.selectedRow == -1) detectRowFromMousePoint(src, e) else src.selectedRow
      if (selectedRow != -1) {
        val pathForRow = src.tree.getPathForRow(selectedRow)
        val rowPath = pathForRow?.path
        if (rowPath != null) appendTreePath(rowPath, path)
      }
    } else if (src is Tree) {
      val point = getConvertedMousePoint(e, src)
      if (point != null) {
        val rowForLocation = src.getRowForLocation(point.x, point.y)
        val rowPath = if (rowForLocation > 0) src.getPathForRow(rowForLocation) else null
        if (rowPath != null) {
          appendTreePath(rowPath.path, path)
        }
      }
    }
    //end path
    val text = (src as? AbstractButton)?.text ?: (src as? JLabel)?.text ?: src.name
    appendSrcText(path, text)
    return true
  }

}