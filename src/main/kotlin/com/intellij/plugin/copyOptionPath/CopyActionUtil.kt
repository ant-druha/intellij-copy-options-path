package com.intellij.plugin.copyOptionPath

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.newEditor.SettingsDialog
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.border.IdeaTitledBorder
import com.intellij.ui.tabs.JBTabs
import com.intellij.ui.treeStructure.treetable.TreeTable
import com.intellij.util.ui.UIUtil
import java.awt.Component
import java.awt.Point
import java.awt.event.MouseEvent
import java.lang.reflect.Field
import java.util.*
import javax.swing.JPanel
import javax.swing.SwingUtilities

val LOG: Logger = Logger.getInstance("#com.intellij.plugin.copyOptionPath")

fun appendTreePath(treePath: Array<out Any>, path: StringBuilder) {

  treePath.forEach {
    val pathStr = it.toString()
    if (StringUtil.isEmpty(pathStr)) {
      try {
        val textField = it.javaClass.getDeclaredField("myText")
        if (textField != null) {
          textField.isAccessible = true
          val textValue = textField.get(it)?.toString()
          appendItem(path, textValue)
        }
      } catch (e: Exception) {
        LOG.debug("Error trying to get 'myText' field from $it: ${e.message}")
      }
    } else appendItem(path, pathStr)
  }
}

fun detectRowFromMousePoint(treeTable: TreeTable, e: AnActionEvent): Int {
  val point = getConvertedMousePoint(e, treeTable) ?: return -1
  val rowAtPoint = treeTable.rowAtPoint(point)
  return if (rowAtPoint <= treeTable.rowCount) rowAtPoint else -1
}

fun getConvertedMousePoint(event: AnActionEvent, destination: Component): Point? {
  val e = event.inputEvent
  if (e is MouseEvent) {
    return SwingUtilities.convertMouseEvent(e.component, e, destination).point
  }
  return null
}

fun getMiddlePath(src: Component, path: StringBuilder) {
  val jbTabs = UIUtil.getParentOfType(JBTabs::class.java, src)
  if (jbTabs != null) {
    val text = jbTabs.selectedInfo?.text
    if (StringUtil.isNotEmpty(text)) path.append("$text | ")
  }
  val parent = src.parent
  val title: String
  if (parent is JPanel) {
    val border = parent.border
    title = if (border is IdeaTitledBorder) border.title else ""
    if (StringUtil.isNotEmpty(title)) appendItem(path, title)
  }
}

fun appendItem(path: StringBuilder, item: String?) {
  if (StringUtil.isNotEmpty(item) && !path.trimEnd { c -> c == ' ' || c == '|' }.endsWith(item!!)) path.append("$item | ")
}

fun appendSrcText(path: StringBuilder, text: String?) {
  if (StringUtil.isNotEmpty(text)) path.append(text)
}

fun trimFinalResult(path: StringBuilder): String {
  val text = path.toString().trimEnd { c -> c == '|' || c == ' ' }
  return text.deleteTag("html").deleteTag("br").deleteTag("b")
}

fun getInheritedPrivateField(type: Class<Any>, name: String, orClassName: String?): Field? {
  var i: Class<Any> = type
  while (i != Object::class.java) {
    for (f in i.declaredFields) {
      if (name == f.name || (orClassName != null && f.type.name == orClassName)) return f
    }
    i = i.getSuperclass()
  }

  return null
}

fun getPathFromSettingsDialog(settings: SettingsDialog): String? {
  try {
    val editor = getInheritedPrivateField(settings.javaClass, "myEditor",
        "com.intellij.openapi.options.newEditor.AbstractEditor") ?: return null
    editor.isAccessible = true
    val settingsEditorInstance = editor.get(settings) as? JPanel ?: return ""
    val banner = getInheritedPrivateField(settingsEditorInstance.javaClass, "myBanner",
        "com.intellij.openapi.options.newEditor.Banner")
        ?: return null
    banner.isAccessible = true
    val bannerInstance = banner.get(settingsEditorInstance)
    val bk = getInheritedPrivateField(bannerInstance.javaClass, "myBreadcrumbs",
        "com.intellij.ui.components.breadcrumbs.Breadcrumbs")
        ?: return null
    bk.isAccessible = true
    val bkViews = bk.type.getDeclaredField("views")
    bkViews.isAccessible = true
    val bkInst = bk.get(bannerInstance)
    val views = bkViews.get(bkInst) as?ArrayList<*>
    var path = "Settings (Preferences on macOS) | "
    views?.forEachIndexed { i, cr ->
      val text = cr.javaClass.getDeclaredField("text")
      text.isAccessible = true
      val value = text.get(cr)
      if (value != null) path += (if (i > 0) " | " else "") + value
    }
    return path

  } catch (e: Exception) {
    LOG.debug("Exception when appending path: ${e.message}")
    return ""
  }
}

fun appendPathFromProjectStructureDialog(configurable: Configurable, path: StringBuilder) {
  if (configurable is ProjectStructureConfigurable) {
    val place = configurable.history.query()
    val structureCfg = place.getPath(ProjectStructureConfigurable.CATEGORY)
    val structurePath = (structureCfg as? Configurable)?.displayName ?: ""
    appendItem(path, structurePath)
  }
}

private fun String.deleteTag(tag: String): String {
  return this.replace("<$tag>", "").replace("</$tag>", "")
}