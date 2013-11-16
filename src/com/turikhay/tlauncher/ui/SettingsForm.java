package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.util.U;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JLabel;

public class SettingsForm extends CenterPanel implements LoginListener {
   private static final long serialVersionUID = -4851979612103757573L;
   final GameDirectoryField gameDirField = new GameDirectoryField(this);
   final ResolutionField resolutionField;
   final JavaExecutableField pathCustomField;
   final AutologinTimeoutField autologinField;
   final LangChoice langChoice;
   final LaunchActionChoice launchActionChoice;
   final TimeoutField connectionField;
   final LocalizableLabel gameDirCustom;
   final LocalizableLabel resolutionCustom;
   final LocalizableLabel pathCustom;
   final LocalizableLabel argsCustom;
   final LocalizableLabel tlauncherSettings;
   final LocalizableLabel autologinCustom;
   final LocalizableLabel launchActionCustom;
   final LocalizableLabel connTimeoutLabel;
   final JLabel langCustom;
   final ArgsField javaArgsField;
   final ArgsField minecraftArgsField;
   final SettingsCheckbox snapshotsSelect;
   final SettingsCheckbox betaSelect;
   final SettingsCheckbox alphaSelect;
   final SettingsCheckbox cheatsSelect;
   final SettingsCheckbox consoleSelect;
   final SettingsCheckbox sunSelect;
   final LocalizableLabel versionChoice;
   final LocalizableButton defButton;
   final LocalizableButton saveButton;
   final SettingsPanel settingsPan = new SettingsPanel(this);
   final VersionsPanel versionsPan;
   final TLauncherSettingsPanel tlauncherPan;
   final ArgsPanel argsPan;
   final SaveSettingsPanel savePan;
   final FocusListener warner = new FocusListener() {
      public void focusGained(FocusEvent e) {
         SettingsForm.this.error_l.setText("settings.warning");
      }

      public void focusLost(FocusEvent e) {
         SettingsForm.this.error_l.setText(" ");
      }
   };
   final FocusListener restart = new FocusListener() {
      public void focusGained(FocusEvent e) {
         SettingsForm.this.error_l.setText("settings.restart");
      }

      public void focusLost(FocusEvent e) {
         SettingsForm.this.error_l.setText(" ");
      }
   };
   private String oldDir;
   private boolean snapshot_old;
   private boolean snapshot_changed;
   private boolean beta_old;
   private boolean beta_changed;
   private boolean alpha_old;
   private boolean alpha_changed;
   private boolean cheats_old;
   private boolean cheats_changed;

   public SettingsForm(TLauncherFrame tlauncher) {
      super(tlauncher);
      this.gameDirField.addFocusListener(this.warner);
      this.resolutionField = new ResolutionField(this);
      this.pathCustomField = new JavaExecutableField(this);
      this.pathCustomField.addFocusListener(this.warner);
      this.autologinField = new AutologinTimeoutField(this);
      this.langChoice = new LangChoice(this);
      this.launchActionChoice = new LaunchActionChoice(this);
      this.connectionField = new TimeoutField(this, TimeoutField.FieldType.CONNECTION);
      this.gameDirCustom = new LocalizableLabel("settings.client.gamedir.label");
      this.resolutionCustom = new LocalizableLabel("settings.client.resolution.label");
      this.versionChoice = new LocalizableLabel("settings.versions.label");
      this.snapshotsSelect = new SettingsCheckbox(this, "settings.versions.snapshots", "minecraft.versions.snapshots");
      this.snapshotsSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean selected = e.getStateChange() == 1;
            SettingsForm.this.snapshot_changed = SettingsForm.this.snapshot_old ^ selected;
         }
      });
      this.betaSelect = new SettingsCheckbox(this, "settings.versions.beta", "minecraft.versions.beta");
      this.betaSelect.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean newstate) {
            SettingsForm.this.beta_changed = SettingsForm.this.beta_old ^ newstate;
         }
      });
      this.alphaSelect = new SettingsCheckbox(this, "settings.versions.alpha", "minecraft.versions.alpha");
      this.alphaSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean selected = e.getStateChange() == 1;
            SettingsForm.this.alpha_changed = SettingsForm.this.alpha_old ^ selected;
         }
      });
      this.cheatsSelect = new SettingsCheckbox(this, "settings.versions.cheats", "minecraft.versions.cheats");
      this.cheatsSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean selected = e.getStateChange() == 1;
            SettingsForm.this.cheats_changed = SettingsForm.this.cheats_old ^ selected;
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
      this.consoleSelect = new SettingsCheckbox("settings.tlauncher.console", "gui.console", false);
      this.sunSelect = new SettingsCheckbox("settings.tlauncher.sun", "gui.sun", true);
      this.sunSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            if (SettingsForm.this.f.mp != null) {
               switch(e.getStateChange()) {
               case 1:
                  SettingsForm.this.f.mp.startBackground();
                  break;
               case 2:
                  SettingsForm.this.f.mp.stopBackground();
               }

            }
         }
      });
      this.tlauncherPan = new TLauncherSettingsPanel(this);
      this.autologinCustom = new LocalizableLabel("settings.tlauncher.autologin.label");
      this.connTimeoutLabel = new LocalizableLabel("settings.timeouts.connection.label");
      this.langCustom = new JLabel("Language:");
      this.launchActionCustom = new LocalizableLabel("settings.launch-action.label");
      this.saveButton = new LocalizableButton("settings.save");
      this.saveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsForm.this.defocus();
            if (SettingsForm.this.save()) {
               SettingsForm.this.f.mp.toggleSettings();
            }

         }
      });
      this.defButton = new LocalizableButton("settings.default");
      this.defButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsForm.this.defocus();
            if (Alert.showQuestion("settings.setdefault", true)) {
               SettingsForm.this.setToDefaults();
            }
         }
      });
      this.savePan = new SaveSettingsPanel(this);
      this.settingsPan.createInterface();
      this.add(this.error);
      this.add(this.settingsPan);
      this.add(this.savePan);
      this.updateValues();
   }

   public void updateValues() {
      Iterator var5 = this.findFields().iterator();

      while(var5.hasNext()) {
         Component c = (Component)var5.next();
         if (c instanceof SettingsField) {
            SettingsField sf = (SettingsField)c;
            String key = sf.getSettingsPath();
            String val = this.s.get(key);
            sf.setValue(val);
            if (!this.s.isSaveable(key)) {
               c.setEnabled(false);
            } else {
               c.setEnabled(true);
            }
         }
      }

      this.oldDir = this.gameDirField.getValue();
      this.snapshot_changed = this.beta_changed = this.alpha_changed = this.cheats_changed = false;
      this.snapshot_old = this.snapshotsSelect.getState();
      this.alpha_old = this.alphaSelect.getState();
      this.beta_old = this.betaSelect.getState();
      this.cheats_old = this.cheatsSelect.getState();
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
            SettingsField sf = (SettingsField)c;
            if (!sf.isValueValid()) {
               c.setBackground(this.wrongColor);
               return false;
            }

            if (c.isEnabled()) {
               this.s.set(sf.getSettingsPath(), sf.getValue(), false);
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
         if (this.snapshot_changed || this.beta_changed || this.alpha_changed || this.cheats_changed) {
            this.f.lf.versionchoice.asyncRefresh();
         }

         this.snapshot_changed = this.beta_changed = this.alpha_changed = this.cheats_changed = false;
         this.snapshot_old = this.snapshotsSelect.getState();
         this.alpha_old = this.alphaSelect.getState();
         this.beta_old = this.betaSelect.getState();
         this.cheats_old = this.cheatsSelect.getState();
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

   public boolean onLogin() {
      if (this.save()) {
         return true;
      } else {
         this.f.mp.setSettings(true);
         return false;
      }
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }
}
