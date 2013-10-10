package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.util.U;
import java.awt.Button;
import java.awt.Component;
import java.awt.Container;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SettingsForm extends CenterPanel {
   private static final long serialVersionUID = -4851979612103757573L;
   final GameDirectoryField gameDirField = new GameDirectoryField(this);
   final ResolutionField resolutionField;
   final JavaExecutableField pathCustomField;
   final AutologinTimeoutField autologinField;
   final LangChoice langChoice;
   final LaunchActionChoice launchActionChoice;
   final LocalizableLabel gameDirCustom;
   final LocalizableLabel resolutionCustom;
   final LocalizableLabel pathCustom;
   final LocalizableLabel argsCustom;
   final LocalizableLabel tlauncherSettings;
   final LocalizableLabel autologinCustom;
   final LocalizableLabel launchActionCustom;
   final Label langCustom;
   final ArgsField javaArgsField;
   final ArgsField minecraftArgsField;
   final SettingsCheckbox snapshotsSelect;
   final SettingsCheckbox betaSelect;
   final SettingsCheckbox alphaSelect;
   final SettingsCheckbox consoleSelect;
   final SettingsCheckbox sunSelect;
   final SettingsCheckbox updaterSelect;
   final LocalizableLabel versionChoice;
   final Button backButton;
   final Button defButton;
   final SettingsPanel settingsPan = new SettingsPanel(this);
   final VersionsPanel versionsPan;
   final TLauncherSettingsPanel tlauncherPan;
   final ArgsPanel argsPan;
   final FocusListener warner = new FocusListener() {
      public void focusGained(FocusEvent e) {
         SettingsForm.this.error.setText("settings.warning");
      }

      public void focusLost(FocusEvent e) {
         SettingsForm.this.error.setText("");
      }
   };
   final FocusListener restart = new FocusListener() {
      public void focusGained(FocusEvent e) {
         SettingsForm.this.error.setText("settings.restart");
      }

      public void focusLost(FocusEvent e) {
         SettingsForm.this.error.setText("");
      }
   };
   private String oldDir;
   private boolean updater_enabled;
   private boolean snapshot_old;
   private boolean snapshot_changed;
   private boolean beta_old;
   private boolean beta_changed;
   private boolean alpha_old;
   private boolean alpha_changed;

   public SettingsForm(TLauncherFrame tlauncher) {
      super(tlauncher);
      this.gameDirField.addFocusListener(this.warner);
      this.resolutionField = new ResolutionField(this);
      this.pathCustomField = new JavaExecutableField(this);
      this.pathCustomField.addFocusListener(this.warner);
      this.autologinField = new AutologinTimeoutField(this);
      this.langChoice = new LangChoice(this);
      this.launchActionChoice = new LaunchActionChoice(this);
      this.gameDirCustom = new LocalizableLabel("settings.client.gamedir.label");
      this.resolutionCustom = new LocalizableLabel("settings.client.resolution.label");
      this.versionChoice = new LocalizableLabel("settings.versions.label");
      this.snapshotsSelect = new SettingsCheckbox("settings.versions.snapshots", "minecraft.versions.snapshots");
      this.snapshotsSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean selected = e.getStateChange() == 1;
            if (SettingsForm.this.snapshot_old == selected) {
               SettingsForm.this.snapshot_changed = false;
            } else {
               SettingsForm.this.snapshot_changed = true;
            }

         }
      });
      this.betaSelect = new SettingsCheckbox("settings.versions.beta", "minecraft.versions.beta");
      this.betaSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean selected = e.getStateChange() == 1;
            if (SettingsForm.this.beta_old == selected) {
               SettingsForm.this.beta_changed = false;
            } else {
               SettingsForm.this.beta_changed = true;
            }

         }
      });
      this.alphaSelect = new SettingsCheckbox("settings.versions.alpha", "minecraft.versions.alpha");
      this.alphaSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean selected = e.getStateChange() == 1;
            if (SettingsForm.this.alpha_old == selected) {
               SettingsForm.this.alpha_changed = false;
            } else {
               SettingsForm.this.alpha_changed = false;
            }

         }
      });
      this.versionsPan = new VersionsPanel(this);
      this.pathCustom = new LocalizableLabel("settings.java.path.label");
      this.argsCustom = new LocalizableLabel("settings.java.args.label");
      this.javaArgsField = new ArgsField(this, "settings.java.args.jvm", "minecraft.javaargs");
      this.javaArgsField.addFocusListener(this.warner);
      this.minecraftArgsField = new ArgsField(this, "settings.java.args.minecraft", "minecraft.args");
      this.minecraftArgsField.addFocusListener(this.warner);
      this.argsPan = new ArgsPanel(this);
      this.tlauncherSettings = new LocalizableLabel("settings.tlauncher.label");
      this.consoleSelect = new SettingsCheckbox("settings.tlauncher.console", "gui.console");
      this.updaterSelect = new SettingsCheckbox("settings.tlauncher.updater", "updater.enabled");
      this.updaterSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            switch(e.getStateChange()) {
            case 1:
               SettingsForm.this.t.getUpdater().setEnabled(true);
               break;
            case 2:
               SettingsForm.this.t.getUpdater().setEnabled(false);
            }

         }
      });
      this.sunSelect = new SettingsCheckbox("settings.tlauncher.sun", "gui.sun");
      this.sunSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            switch(e.getStateChange()) {
            case 1:
               SettingsForm.this.f.mc.startBackground();
               break;
            case 2:
               SettingsForm.this.f.mc.stopBackground();
            }

         }
      });
      this.tlauncherPan = new TLauncherSettingsPanel(this);
      this.autologinCustom = new LocalizableLabel("settings.tlauncher.autologin.label");
      this.langCustom = new Label("Language:");
      this.launchActionCustom = new LocalizableLabel("settings.launch-action.label");
      this.backButton = new LocalizableButton("settings.back");
      this.backButton.setFont(this.font_bold);
      this.backButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (SettingsForm.this.save()) {
               SettingsForm.this.goBack();
            }

         }
      });
      this.defButton = new LocalizableButton("settings.default");
      this.defButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (Alert.showQuestion("settings.setdefault", true)) {
               SettingsForm.this.setToDefaults();
            }
         }
      });
      this.settingsPan.createInterface();
      this.add(this.error);
      this.add(this.settingsPan);
      this.add(this.backButton);
      this.add(this.defButton);
      this.updateValues();
   }

   public void updateValues() {
      U.log("Updating values...");
      Iterator var5 = this.findFields().iterator();

      while(var5.hasNext()) {
         Component c = (Component)var5.next();
         if (c instanceof SettingsField) {
            U.log("> Filling " + c.getClass().getName() + "...");
            SettingsField sf = (SettingsField)c;
            String key = sf.getSettingsPath();
            String val = this.s.get(key);
            U.log("Key:", key, "Value:", val);
            sf.setValue(val);
            if (!this.s.isSaveable(key)) {
               U.log("Field is unsaveable!");
               c.setEnabled(false);
            } else {
               c.setEnabled(true);
            }
         }
      }

      this.updater_enabled = this.updaterSelect.getState();
      this.oldDir = this.gameDirField.getValue();
      this.snapshot_old = this.snapshotsSelect.getState();
      this.alpha_old = this.alphaSelect.getState();
      this.beta_old = this.betaSelect.getState();
   }

   public void setToDefaults() {
      Iterator var2 = this.findFields().iterator();

      while(var2.hasNext()) {
         Component c = (Component)var2.next();
         if (c instanceof SettingsField) {
            ((SettingsField)c).setToDefault();
         }
      }

   }

   public boolean save() {
      U.log("Saving...");
      Iterator var3 = this.findFields().iterator();

      while(var3.hasNext()) {
         Component c = (Component)var3.next();
         if (c instanceof SettingsField) {
            U.log("> Checking " + c.getClass().getSimpleName() + "...");
            SettingsField sf = (SettingsField)c;
            if (!sf.isValueValid()) {
               U.log("Invalid!");
               c.setBackground(this.wrongColor);
               return false;
            }

            if (!c.isEnabled()) {
               U.log("Is insaveable!");
            } else {
               this.s.set(sf.getSettingsPath(), sf.getValue(), false);
               U.log("Saved (", sf.getSettingsPath(), ":", sf.getValue(), ")");
            }
         }
      }

      try {
         this.s.save();
         U.log("Settings saved!");
      } catch (IOException var5) {
         U.log("Cannot save settings!");
         Alert.showError(var5, false);
      }

      if (!this.updater_enabled && this.updaterSelect.getState()) {
         this.t.getUpdater().asyncFindUpdate();
      }

      if (this.langChoice.changed) {
         this.langChoice.setCurrent();
         U.log("Language has been changed.");
         this.f.updateLocales();
      }

      String gamedir = this.gameDirField.getValue();
      if (!gamedir.equals(this.oldDir)) {
         this.oldDir = gamedir;
         U.log("Game directory has been changed. Recreating Version Manager.");

         try {
            this.t.getVersionManager().recreate();
         } catch (IOException var4) {
            Alert.showError(var4, false);
         }

         return true;
      } else {
         if (this.snapshot_changed || this.beta_changed || this.alpha_changed) {
            this.f.lf.versionchoice.asyncRefresh();
         }

         this.snapshot_changed = this.beta_changed = this.alpha_changed = false;
         return true;
      }
   }

   private List findFields(Container container) {
      List f = new ArrayList();
      Component[] var6;
      int var5 = (var6 = container.getComponents()).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Component c = var6[var4];
         if (c instanceof SettingsField) {
            f.add(c);
         } else if (c instanceof Container) {
            f.addAll(this.findFields((Container)c));
         }
      }

      return f;
   }

   private List findFields() {
      return this.findFields(this);
   }

   private void goBack() {
      this.f.mc.showLogin();
   }

   protected void blockElement(Object reason) {
   }

   protected void unblockElement(Object reason) {
   }
}
