package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.util.U;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class SettingsForm extends CenterPanel {
   private static final long serialVersionUID = -4851979612103757573L;
   final GameDirectoryField gameDirField = new GameDirectoryField(this);
   final ResolutionField resolutionField;
   final JavaDirectoryField pathCustomField;
   final AutologinTimeoutField autologinField;
   final LangChoice langChoice;
   final Label gameDirCustom;
   final Label resolutionCustom;
   final Label pathCustom;
   final Label argsCustom;
   final Label tlauncherSettings;
   final Label autologinCustom;
   final Label langCustom;
   final ArgsField javaArgsField;
   final ArgsField minecraftArgsField;
   final Checkbox snapshotsSelect;
   final Checkbox betaSelect;
   final Checkbox alphaSelect;
   final Checkbox consoleSelect;
   final Checkbox sunSelect;
   final Label versionChoice;
   final Button backButton;
   final Button defButton;
   final SettingsPanel settingsPan = new SettingsPanel(this);
   final VersionsPanel versionsPan;
   final TLauncherSettingsPanel tlauncherPan;
   final ArgsPanel argsPan;
   final FocusListener warner = new FocusListener() {
      public void focusGained(FocusEvent e) {
         SettingsForm.this.error.setText(SettingsForm.this.l.get("settings.warning"));
      }

      public void focusLost(FocusEvent e) {
         SettingsForm.this.error.setText("");
      }
   };
   final FocusListener restart = new FocusListener() {
      public void focusGained(FocusEvent e) {
         SettingsForm.this.error.setText(SettingsForm.this.l.get("settings.restart"));
      }

      public void focusLost(FocusEvent e) {
         SettingsForm.this.error.setText("");
      }
   };
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
      this.pathCustomField = new JavaDirectoryField(this);
      this.pathCustomField.addFocusListener(this.warner);
      this.autologinField = new AutologinTimeoutField(this);
      this.langChoice = new LangChoice(this);
      this.gameDirCustom = new Label(this.l.get("settings.client.gamedir.label"));
      this.resolutionCustom = new Label(this.l.get("settings.client.resolution.label"));
      this.versionChoice = new Label(this.l.get("settings.versions.label"));
      this.snapshotsSelect = new Checkbox(this.l.get("settings.versions.snapshots"));
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
      this.betaSelect = new Checkbox(this.l.get("settings.versions.beta"));
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
      this.alphaSelect = new Checkbox(this.l.get("settings.versions.alpha"));
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
      this.pathCustom = new Label(this.l.get("settings.java.path.label"));
      this.argsCustom = new Label(this.l.get("settings.java.args.label"));
      this.javaArgsField = new ArgsField(this, this.l.get("settings.java.args.jvm"));
      this.javaArgsField.addFocusListener(this.warner);
      this.minecraftArgsField = new ArgsField(this, this.l.get("settings.java.args.minecraft"));
      this.minecraftArgsField.addFocusListener(this.warner);
      this.argsPan = new ArgsPanel(this);
      this.tlauncherSettings = new Label(this.l.get("settings.tlauncher.label"));
      this.consoleSelect = new Checkbox(this.l.get("settings.tlauncher.console"));
      this.sunSelect = new Checkbox(this.l.get("settings.tlauncher.sun"));
      this.sunSelect.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            switch(e.getStateChange()) {
            case 1:
               SettingsForm.this.f.mc.sun.allow();
               break;
            case 2:
               SettingsForm.this.f.mc.sun.cancel();
            }

         }
      });
      this.tlauncherPan = new TLauncherSettingsPanel(this);
      this.autologinCustom = new Label(this.l.get("settings.tlauncher.autologin.label"));
      this.langCustom = new Label(this.l.get("settings.lang.label"));
      this.langChoice.addFocusListener(this.restart);
      this.backButton = new Button(this.l.get("settings.back"));
      this.backButton.setFont(this.font_bold);
      this.backButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (SettingsForm.this.save()) {
               SettingsForm.this.goBack();
            }

         }
      });
      this.defButton = new Button(this.l.get("settings.default"));
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
      String gamedir = this.s.get("minecraft.gamedir");
      String javadir = this.s.get("minecraft.javadir");
      String javaargs = this.s.get("minecraft.javaargs");
      String args = this.s.get("minecraft.args");
      String locale = this.s.get("locale");
      int resW = this.s.getInteger("minecraft.size.width");
      int resH = this.s.getInteger("minecraft.size.height");
      int autologin = this.s.getInteger("login.auto.timeout");
      boolean snapshots = this.snapshot_old = this.s.getBoolean("minecraft.versions.snapshots");
      boolean beta = this.beta_old = this.s.getBoolean("minecraft.versions.beta");
      boolean alpha = this.alpha_old = this.s.getBoolean("minecraft.versions.alpha");
      boolean console = this.s.getBoolean("gui.console");
      boolean sun = this.s.getBoolean("gui.sun");
      this.gameDirField.setText(gamedir);
      this.resolutionField.setValues(resW, resH);
      this.pathCustomField.setText(javadir);
      this.javaArgsField.setText(javaargs);
      this.minecraftArgsField.setText(args);
      this.langChoice.selectValue(locale);
      this.snapshotsSelect.setState(snapshots);
      this.betaSelect.setState(beta);
      this.alphaSelect.setState(alpha);
      this.consoleSelect.setState(console);
      this.sunSelect.setState(sun);
      this.autologinField.setText(autologin);
   }

   public void setToDefaults() {
      this.gameDirField.setText((String)null);
      this.resolutionField.setValues(0, 0);
      this.pathCustomField.setText((String)null);
      this.javaArgsField.setText((String)null);
      this.minecraftArgsField.setText((String)null);
      this.snapshotsSelect.setState(true);
      this.betaSelect.setState(true);
      this.alphaSelect.setState(true);
      this.consoleSelect.setState(false);
      this.sunSelect.setState(true);
      this.autologinField.setText(3);
   }

   public boolean save() {
      U.log("Saving settings...");
      String gamedir = this.gameDirField.getValue();
      String javadir = this.pathCustomField.getValue();
      String javaargs = this.javaArgsField.getValue();
      String args = this.minecraftArgsField.getValue();
      String locale = this.langChoice.getValue();
      int autologin = this.autologinField.getSpecialValue();
      int[] size = this.resolutionField.getValues();
      boolean snapshots = this.snapshotsSelect.getState();
      boolean beta = this.betaSelect.getState();
      boolean alpha = this.alphaSelect.getState();
      boolean console = this.consoleSelect.getState();
      boolean sun = this.sunSelect.getState();
      if (gamedir == null) {
         return this.setError(this.l.get("settings.client.gamedir.invalid"));
      } else if (javadir == null) {
         return this.setError(this.l.get("settings.java.path.invalid"));
      } else if (size == null) {
         return this.setError(this.l.get("settings.client.resolution.invalid"));
      } else {
         this.s.set("minecraft.gamedir", gamedir);
         this.s.set("minecraft.javadir", javadir);
         this.s.set("minecraft.javaargs", javaargs);
         this.s.set("minecraft.args", args);
         this.s.set("locale", locale);
         this.s.set("minecraft.size.width", size[0]);
         this.s.set("minecraft.size.height", size[1]);
         this.s.set("minecraft.versions.snapshots", snapshots);
         this.s.set("minecraft.versions.beta", beta);
         this.s.set("minecraft.versions.alpha", alpha);
         this.s.set("gui.console", console);
         this.s.set("gui.sun", sun);
         this.s.set("login.auto.timeout", autologin);
         U.log("Settings saved!");
         if (this.snapshot_changed || this.beta_changed || this.alpha_changed) {
            this.f.lf.versionchoice.asyncRefresh();
         }

         return true;
      }
   }

   private void goBack() {
      this.f.mc.showLogin();
   }

   protected void blockElement(Object reason) {
   }

   protected void unblockElement(Object reason) {
   }
}
