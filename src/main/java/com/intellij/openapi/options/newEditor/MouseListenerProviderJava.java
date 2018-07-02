package com.intellij.openapi.options.newEditor;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.util.ui.UIUtil;

import java.awt.*;

public class MouseListenerProviderJava implements ApplicationComponent {
  @Override
  public void initComponent() {
    addSettingsListeners();
  }

  private void addSettingsListeners() {
    ActionManager.getInstance().addAnActionListener((action, dataContext, event) -> {
      Component component = dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT);
//      SettingsEditor settingsEditor = UIUtil.getParentOfType(SettingsEditor.class, component);
//      settingsEditor.addOptionsListener(new OptionsEditorColleague.Adapter());
    });
//   *       IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown({
//          *         val settings = DialogWrapper.findInstance(IdeFocusManager.findInstance().focusOwner)
//          *         if (settings is SettingsDialog) {
//   *           val contentPanel = settings.contentPanel
//            *           val contentPane = settings.contentPane
//            * //        settings.doOKAction()
//   *
//   *         }
//   *
//   *       }, ModalityState.current())
//   *     }
  }

  /**
   *   private fun addSettingsListeners() {
   *     ActionManager.getInstance().addAnActionListener { action, dataContext, event ->
   *       val inputEvent = event.inputEvent
   *       val component = dataContext.getData(PlatformDataKeys.CONTEXT_COMPONENT)
   *       val settingsEditor = UIUtil.getParentOfType(SettingsEditor::class.java, component)
   *       IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown({
   *         val settings = DialogWrapper.findInstance(IdeFocusManager.findInstance().focusOwner)
   *         if (settings is SettingsDialog) {
   *           val contentPanel = settings.contentPanel
   *           val contentPane = settings.contentPane
   * //        settings.doOKAction()
   *
   *         }
   *
   *       }, ModalityState.current())
   *     }
   *     IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown({
   *       val settings = DialogWrapper.findInstance(IdeFocusManager.findInstance().focusOwner)
   *       if (settings is SettingsDialog) {
   * //        settings.doOKAction()
   *
   *       }
   *
   *     }, ModalityState.current())
   *   }
   */
}
