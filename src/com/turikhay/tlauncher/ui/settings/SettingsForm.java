package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.tlauncher.ui.CheckBoxListener;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.loc.LocalizableButton;
import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import com.turikhay.tlauncher.ui.login.LoginListener;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.util.U;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JLabel;

public class SettingsForm extends CenterPanel implements LoginListener {
   private static final long serialVersionUID = -4851979612103757573L;
   private final DefaultScene scene;
   final GameDirectoryField gameDirField;
   final ResolutionField resolutionField;
   final JavaExecutableField pathCustomField;
   final AutologinTimeoutField autologinField;
   final LangChoice langChoice;
   final LaunchActionChoice launchActionChoice;
   final LocalizableLabel gameDirCustom;
   final LocalizableLabel resolutionCustom;
   final LocalizableLabel pathCustom;
   final LocalizableLabel argsCustom;
   final LocalizableLabel consoleSettings;
   final LocalizableLabel autologinCustom;
   final LocalizableLabel launchActionCustom;
   final LocalizableLabel connQualityLabel;
   final JLabel langCustom;
   final ArgsField javaArgsField;
   final ArgsField minecraftArgsField;
   final SettingsCheckbox snapshotsSelect;
   final SettingsCheckbox betaSelect;
   final SettingsCheckbox alphaSelect;
   final SettingsCheckbox cheatsSelect;
   final SettingsCheckbox oldSelect;
   final LocalizableLabel versionChoice;
   final SettingsRadioButton globalConsole;
   final SettingsRadioButton minecraftConsole;
   final SettingsRadioButton noneConsole;
   final ConsoleSettingsPanel consolePan;
   final SettingsRadioButton goodQuality;
   final SettingsRadioButton normalQuality;
   final SettingsRadioButton badQuality;
   final ConnectionQualitySettingsPanel connPan;
   final LocalizableButton defButton;
   final LocalizableButton saveButton;
   final SettingsPanel settingsPan;
   final VersionsPanel versionsPan;
   final ArgsPanel argsPan;
   final SaveSettingsPanel savePan;
   final FocusListener warner;
   final FocusListener restart;
   private String oldDir;
   private boolean snapshot_old;
   private boolean snapshot_changed;
   private boolean beta_old;
   private boolean beta_changed;
   private boolean alpha_old;
   private boolean alpha_changed;
   private boolean cheats_old;
   private boolean cheats_changed;
   private boolean old_old;
   private boolean old_changed;

   public SettingsForm(DefaultScene sc) {
      this.scene = sc;
      this.warner = new FocusListener() {
         public void focusGained(FocusEvent e) {
            SettingsForm.this.setError("settings.warning");
         }

         public void focusLost(FocusEvent e) {
            SettingsForm.this.setError(" ");
         }
      };
      this.restart = new FocusListener() {
         public void focusGained(FocusEvent e) {
            SettingsForm.this.setError("settings.restart");
         }

         public void focusLost(FocusEvent e) {
            SettingsForm.this.setError(" ");
         }
      };
      this.settingsPan = new SettingsPanel(this);
      this.gameDirField = new GameDirectoryField(this);
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
      this.snapshotsSelect = new SettingsCheckbox(this, "settings.versions.snapshots", "minecraft.versions.snapshots");
      this.snapshotsSelect.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean e) {
            SettingsForm.this.snapshot_changed = SettingsForm.this.snapshot_old ^ e;
         }
      });
      this.betaSelect = new SettingsCheckbox(this, "settings.versions.beta", "minecraft.versions.beta");
      this.betaSelect.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean e) {
            SettingsForm.this.beta_changed = SettingsForm.this.beta_old ^ e;
         }
      });
      this.alphaSelect = new SettingsCheckbox(this, "settings.versions.alpha", "minecraft.versions.alpha");
      this.alphaSelect.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean e) {
            SettingsForm.this.alpha_changed = SettingsForm.this.alpha_old ^ e;
         }
      });
      this.cheatsSelect = new SettingsCheckbox(this, "settings.versions.cheats", "minecraft.versions.cheats");
      this.cheatsSelect.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean e) {
            SettingsForm.this.cheats_changed = SettingsForm.this.cheats_old ^ e;
         }
      });
      this.oldSelect = new SettingsCheckbox(this, "settings.versions.old", "minecraft.versions.old");
      this.oldSelect.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean e) {
            SettingsForm.this.old_changed = SettingsForm.this.old_old ^ e;
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
      this.consoleSettings = new LocalizableLabel("settings.console.label");
      this.globalConsole = new SettingsRadioButton("settings.console.global", "global");
      this.globalConsole.addItemListener(new CheckBoxListener() {
         public void itemStateChanged(boolean newstate) {
            TLauncher.getConsole().setShown(newstate);
         }
      });
      this.minecraftConsole = new SettingsRadioButton("settings.console.minecraft", "minecraft");
      this.noneConsole = new SettingsRadioButton("settings.console.none", "none");
      this.consolePan = new ConsoleSettingsPanel(this);
      this.autologinCustom = new LocalizableLabel("settings.tlauncher.autologin.label");
      this.connQualityLabel = new LocalizableLabel("settings.connection.label");
      this.goodQuality = new SettingsRadioButton("settings.connection.good", "good");
      this.normalQuality = new SettingsRadioButton("settings.connection.normal", "normal");
      this.badQuality = new SettingsRadioButton("settings.connection.bad", "bad");
      this.connPan = new ConnectionQualitySettingsPanel(this);
      this.langCustom = new JLabel("Language:");
      this.launchActionCustom = new LocalizableLabel("settings.launch-action.label");
      this.saveButton = new LocalizableButton("settings.save");
      this.saveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsForm.this.defocus();
            if (SettingsForm.this.save()) {
               SettingsForm.this.scene.toggleSettings();
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
            String val = this.global.get(key);
            sf.setValue(val);
         }
      }

      this.oldDir = this.gameDirField.getValue();
      this.snapshot_changed = this.beta_changed = this.alpha_changed = this.cheats_changed = this.old_changed = false;
      this.snapshot_old = this.snapshotsSelect.getState();
      this.alpha_old = this.alphaSelect.getState();
      this.beta_old = this.betaSelect.getState();
      this.cheats_old = this.cheatsSelect.getState();
      this.old_old = this.oldSelect.getState();
   }

   public void updateIfSaveable() {
      Iterator var3 = this.findFields().iterator();

      while(var3.hasNext()) {
         Component c = (Component)var3.next();
         if (c instanceof SettingsField) {
            String key = ((SettingsField)c).getSettingsPath();
            if (!this.global.isSaveable(key)) {
               c.setEnabled(false);
            }
         }
      }

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
               c.setBackground(this.getTheme().getFailure());
               return false;
            }

            if (c.isEnabled()) {
               this.global.set(sf.getSettingsPath(), sf.getValue(), false);
            }
         }
      }

      try {
         this.global.save();
         U.log("Settings saved!");
      } catch (IOException var5) {
         U.log("Cannot save settings!");
         Alert.showError(var5, false);
      }

      if (this.langChoice.changed) {
         this.langChoice.setCurrent();
         U.log("Language has been changed.");
         this.tlauncher.getFrame().updateLocales();
      }

      String gamedir = this.gameDirField.getValue();
      if (!gamedir.equals(this.oldDir)) {
         this.oldDir = gamedir;
         U.log("Game directory has been changed. Recreating Version Manager.");

         try {
            this.tlauncher.getVersionManager().recreate();
         } catch (IOException var4) {
            Alert.showError(var4, false);
         }

         U.log("And Profile Manager, too");
         this.tlauncher.getProfileManager().recreate();
         this.tlauncher.getProfileManager().loadProfiles();
         return true;
      } else {
         if (this.snapshot_changed || this.beta_changed || this.alpha_changed || this.cheats_changed || this.old_changed) {
            this.scene.loginForm.versionchoice.asyncRefresh();
         }

         this.snapshot_changed = this.beta_changed = this.alpha_changed = this.cheats_changed = this.old_changed = false;
         this.snapshot_old = this.snapshotsSelect.getState();
         this.alpha_old = this.alphaSelect.getState();
         this.beta_old = this.betaSelect.getState();
         this.cheats_old = this.cheatsSelect.getState();
         this.old_old = this.oldSelect.getState();
         this.tlauncher.getDownloader().loadConfiguration(this.global);
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
         this.scene.setSettings(true);
         return false;
      }
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }
}
