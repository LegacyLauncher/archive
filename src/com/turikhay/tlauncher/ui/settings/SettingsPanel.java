package com.turikhay.tlauncher.ui.settings;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.managers.VersionLists;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.converter.ActionOnLaunchConverter;
import com.turikhay.tlauncher.ui.converter.ConnectionQualityConverter;
import com.turikhay.tlauncher.ui.converter.ConsoleTypeConverter;
import com.turikhay.tlauncher.ui.converter.LocaleConverter;
import com.turikhay.tlauncher.ui.explorer.FileExplorer;
import com.turikhay.tlauncher.ui.explorer.ImageFileExplorer;
import com.turikhay.tlauncher.ui.loc.LocalizableButton;
import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import com.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import com.turikhay.tlauncher.ui.login.LoginException;
import com.turikhay.tlauncher.ui.login.LoginListener;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import com.turikhay.util.OS;
import com.turikhay.util.U;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import net.minecraft.launcher.versions.ReleaseType;

public class SettingsPanel extends CenterPanel implements LoginListener {
   private static final long serialVersionUID = 3896900830909661270L;
   private static final int PANELS = 5;
   private final DefaultScene scene;
   private final ExtendedPanel container;
   private final ExtendedPanel[] panels;
   private final GridBagConstraints[] constraints;
   private final SettingsFieldHandler directory;
   private final SettingsFieldHandler resolution;
   private final SettingsFieldHandler javaPath;
   private final SettingsFieldHandler javaArgs;
   private final SettingsFieldHandler args;
   private final SettingsFieldHandler slide;
   private final SettingsFieldHandler console;
   private final SettingsFieldHandler connection;
   private final SettingsFieldHandler action;
   private final SettingsFieldHandler lang;
   private final SettingsGroupHandler versionHandler;
   private final LocalizableButton saveButton;
   private final LocalizableButton defaultButton;
   private final List handlers;
   private final JPopupMenu popup;
   private final LocalizableMenuItem infoItem;
   private final LocalizableMenuItem defaultItem;
   private SettingsHandler selectedHandler;

   public SettingsPanel(DefaultScene sc) {
      super(new Insets(0, 15, 15, 15));
      this.scene = sc;
      FocusListener warning = new FocusListener() {
         public void focusGained(FocusEvent e) {
            SettingsPanel.this.setMessage("settings.warning");
         }

         public void focusLost(FocusEvent e) {
            SettingsPanel.this.setMessage((String)null);
         }
      };
      FocusListener restart = new FocusListener() {
         public void focusGained(FocusEvent e) {
            SettingsPanel.this.setMessage("settings.restart");
         }

         public void focusLost(FocusEvent e) {
            SettingsPanel.this.setMessage((String)null);
         }
      };
      this.container = new ExtendedPanel();
      this.container.setLayout(new BoxLayout(this.container, 3));
      this.panels = new ExtendedPanel[5];
      this.constraints = new GridBagConstraints[5];

      for(int i = 0; i < 5; ++i) {
         this.panels[i] = new ExtendedPanel(new GridBagLayout());
         this.panels[i].getInsets().set(0, 0, 0, 0);
         this.constraints[i] = new GridBagConstraints();
         this.constraints[i].fill = 2;
         this.container.add(this.panels[i], this.del(0));
      }

      this.handlers = new ArrayList();
      byte pane = 0;
      byte row = 0;
      this.directory = new SettingsFieldHandler("minecraft.gamedir", new SettingsFileField("settings.client.gamedir.prompt", new FileExplorer(1, true)), warning);
      this.directory.addListener(new SettingsFieldChangeListener() {
         protected void onChange(String oldValue, String newValue) {
            if (SettingsPanel.this.tlauncher.isReady()) {
               try {
                  ((VersionLists)SettingsPanel.this.tlauncher.getManager().getComponent(VersionLists.class)).updateLocal();
               } catch (IOException var4) {
                  Alert.showLocError("settings.client.gamedir.noaccess", var4);
                  return;
               }

               SettingsPanel.this.tlauncher.getVersionManager().asyncRefresh();
               SettingsPanel.this.tlauncher.getProfileManager().recreate();
            }
         }
      });
      byte row = (byte)(row + 1);
      this.add(pane, row, new SettingsPair("settings.client.gamedir.label", new SettingsHandler[]{this.directory}));
      this.resolution = new SettingsFieldHandler("minecraft.size", new SettingsResolutionField("settings.client.resolution.width", "settings.client.resolution.height", this.global), restart);
      this.add(pane, row++, new SettingsPair("settings.client.resolution.label", new SettingsHandler[]{this.resolution}));
      byte pane = (byte)(pane + 1);
      row = 0;
      ReleaseType[] releaseTypes = ReleaseType.getDefinable();
      SettingsFieldHandler[] versions = new SettingsFieldHandler[releaseTypes.length];

      for(int i = 0; i < releaseTypes.length; ++i) {
         ReleaseType releaseType = releaseTypes[i];
         versions[i] = new SettingsFieldHandler("minecraft.versions." + releaseType, new SettingsCheckBox("settings.versions." + releaseType));
      }

      this.versionHandler = new SettingsGroupHandler(versions);
      this.versionHandler.addListener(new SettingsFieldChangeListener() {
         protected void onChange(String oldvalue, String newvalue) {
            TLauncher.getInstance().getVersionManager().updateVersionList();
         }
      });
      row = (byte)(row + 1);
      this.add(pane, row, new SettingsPair("settings.versions.label", versions));
      ++pane;
      row = 0;
      this.javaArgs = new SettingsFieldHandler("minecraft.javaargs", new SettingsTextField("settings.java.args.jvm", true), warning);
      this.args = new SettingsFieldHandler("minecraft.args", new SettingsTextField("settings.java.args.minecraft", true), warning);
      row = (byte)(row + 1);
      this.add(pane, row, new SettingsPair("settings.java.args.label", new SettingsHandler[]{this.javaArgs, this.args}));
      final boolean isWindows = OS.WINDOWS.isCurrent();
      this.javaPath = new SettingsFieldHandler("minecraft.javadir", new SettingsFileField("settings.java.path.prompt", true, new FileExplorer(isWindows ? 0 : 1, true)) {
         private static final long serialVersionUID = -2220392073262107659L;

         public boolean isValueValid() {
            if (!isWindows) {
               return true;
            } else {
               String path = this.getSettingsValue();
               if (path == null) {
                  return true;
               } else {
                  File javaDir = new File(path);
                  if (javaDir.isFile()) {
                     return true;
                  } else {
                     Alert.showLocAsyncError("settings.java.path.doesnotexist");
                     return false;
                  }
               }
            }
         }
      }, warning);
      this.add(pane, row++, new SettingsPair("settings.java.path.label", new SettingsHandler[]{this.javaPath}));
      ++pane;
      row = 0;
      this.slide = new SettingsFieldHandler("gui.background", new SettingsFileField("settings.slide.list.prompt", true, new ImageFileExplorer()));
      this.slide.addListener(new SettingsFieldChangeListener() {
         protected void onChange(String oldValue, String newValue) {
            if (SettingsPanel.this.tlauncher.isReady()) {
               SettingsPanel.this.tlauncher.getFrame().mp.background.SLIDE_BACKGROUND.getThread().asyncRefreshSlide();
            }
         }
      });
      row = (byte)(row + 1);
      this.add(pane, row, new SettingsPair("settings.slide.list.label", new SettingsHandler[]{this.slide}));
      ++pane;
      row = 0;
      this.console = new SettingsFieldHandler("gui.console", new SettingsComboBox(new ConsoleTypeConverter(), Configuration.ConsoleType.values()));
      this.console.addListener(new SettingsFieldChangeListener() {
         // $FF: synthetic field
         private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$configuration$Configuration$ConsoleType;

         protected void onChange(String oldvalue, String newvalue) {
            if (newvalue != null) {
               switch($SWITCH_TABLE$com$turikhay$tlauncher$configuration$Configuration$ConsoleType()[Configuration.ConsoleType.get(newvalue).ordinal()]) {
               case 1:
                  TLauncher.getConsole().show(false);
                  break;
               case 2:
               case 3:
                  TLauncher.getConsole().hide();
                  break;
               default:
                  throw new IllegalArgumentException("Unknown console type!");
               }

            }
         }

         // $FF: synthetic method
         static int[] $SWITCH_TABLE$com$turikhay$tlauncher$configuration$Configuration$ConsoleType() {
            int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$configuration$Configuration$ConsoleType;
            if (var10000 != null) {
               return var10000;
            } else {
               int[] var0 = new int[Configuration.ConsoleType.values().length];

               try {
                  var0[Configuration.ConsoleType.GLOBAL.ordinal()] = 1;
               } catch (NoSuchFieldError var3) {
               }

               try {
                  var0[Configuration.ConsoleType.MINECRAFT.ordinal()] = 2;
               } catch (NoSuchFieldError var2) {
               }

               try {
                  var0[Configuration.ConsoleType.NONE.ordinal()] = 3;
               } catch (NoSuchFieldError var1) {
               }

               $SWITCH_TABLE$com$turikhay$tlauncher$configuration$Configuration$ConsoleType = var0;
               return var0;
            }
         }
      });
      row = (byte)(row + 1);
      this.add(pane, row, new SettingsPair("settings.console.label", new SettingsHandler[]{this.console}));
      this.connection = new SettingsFieldHandler("connection", new SettingsComboBox(new ConnectionQualityConverter(), Configuration.ConnectionQuality.values()));
      this.connection.addListener(new SettingsFieldChangeListener() {
         protected void onChange(String oldValue, String newValue) {
            SettingsPanel.this.tlauncher.getDownloader().setConfiguration(SettingsPanel.this.global.getConnectionQuality());
         }
      });
      this.add(pane, row++, new SettingsPair("settings.connection.label", new SettingsHandler[]{this.connection}));
      this.action = new SettingsFieldHandler("minecraft.onlaunch", new SettingsComboBox(new ActionOnLaunchConverter(), Configuration.ActionOnLaunch.values()));
      this.add(pane, row++, new SettingsPair("settings.launch-action.label", new SettingsHandler[]{this.action}));
      this.lang = new SettingsFieldHandler("locale", new SettingsComboBox(new LocaleConverter(), this.global.getLocales()));
      this.lang.addListener(new SettingsFieldChangeListener() {
         protected void onChange(String oldvalue, String newvalue) {
            if (SettingsPanel.this.tlauncher.getFrame() != null) {
               SettingsPanel.this.tlauncher.getFrame().updateLocales();
            }

         }
      });
      this.add(pane, row++, new SettingsPair("settings.lang.label", new SettingsHandler[]{this.lang}));
      this.saveButton = new LocalizableButton("settings.save");
      this.saveButton.setFont(this.saveButton.getFont().deriveFont(1));
      this.saveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (SettingsPanel.this.saveValues()) {
               SettingsPanel.this.scene.setSettings(false);
            }

         }
      });
      this.defaultButton = new LocalizableButton("settings.default");
      this.defaultButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsPanel.this.resetValues();
         }
      });
      this.popup = new JPopupMenu();
      this.infoItem = new LocalizableMenuItem("settings.popup.info");
      this.infoItem.setEnabled(false);
      this.popup.add(this.infoItem);
      this.defaultItem = new LocalizableMenuItem("settings.popup.default");
      this.defaultItem.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (SettingsPanel.this.selectedHandler != null) {
               SettingsPanel.this.resetValue(SettingsPanel.this.selectedHandler);
            }
         }
      });
      this.popup.add(this.defaultItem);
      Iterator var10 = this.handlers.iterator();

      while(var10.hasNext()) {
         final SettingsHandler handler = (SettingsHandler)var10.next();
         Component handlerComponent = handler.getComponent();
         handlerComponent.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
               if (e.getButton() == 3) {
                  SettingsPanel.this.callPopup(e, handler);
               }
            }
         });
      }

      this.container.add((Component)sepPan(new Component[]{this.saveButton, this.defaultButton}));
      JScrollPane scrollPane = new JScrollPane(this.container);
      scrollPane.setOpaque(false);
      scrollPane.getViewport().setOpaque(false);
      scrollPane.setBorder((Border)null);
      scrollPane.setHorizontalScrollBarPolicy(30);
      scrollPane.setVerticalScrollBarPolicy(20);
      this.add(this.messagePanel, scrollPane);
      this.updateValues();
   }

   void add(int pane, int row, SettingsPair pair) {
      LocalizableLabel label = pair.getLabel();
      ExtendedPanel field = pair.getPanel();
      ExtendedPanel panel = this.panels[pane];
      GridBagConstraints c = this.constraints[pane];
      c.anchor = 17;
      c.gridy = row;
      c.gridx = 0;
      c.weightx = 0.1D;
      panel.add(label, c);
      c.anchor = 13;
      c.gridy = row++;
      c.gridx = 1;
      c.weightx = 1.0D;
      panel.add(field, c);
      Collections.addAll(this.handlers, pair.getHandlers());
   }

   boolean checkValues() {
      boolean allValid = true;
      Iterator var3 = this.handlers.iterator();

      while(var3.hasNext()) {
         SettingsHandler handler = (SettingsHandler)var3.next();
         boolean valid = handler.isValid();
         handler.getComponent().setBackground(valid ? this.getTheme().getBackground() : this.getTheme().getFailure());
         if (!valid) {
            allValid = false;
         }
      }

      return allValid;
   }

   void updateValues() {
      boolean globalUnSaveable = !this.global.isSaveable();
      Iterator var3 = this.handlers.iterator();

      while(true) {
         SettingsHandler handler;
         String path;
         do {
            if (!var3.hasNext()) {
               return;
            }

            handler = (SettingsHandler)var3.next();
            path = handler.getPath();
            String value = this.global.get(path);
            handler.updateValue(value);
         } while(!globalUnSaveable && this.global.isSaveable(path));

         Blocker.block((Blockable)handler, (Object)"unsaveable");
      }
   }

   boolean saveValues() {
      if (!this.checkValues()) {
         return false;
      } else {
         Iterator var2 = this.handlers.iterator();

         while(var2.hasNext()) {
            SettingsHandler handler = (SettingsHandler)var2.next();
            String path = handler.getPath();
            String value = handler.getValue();
            this.global.set(path, value, false);
            handler.onChange(value);
         }

         this.global.store();
         return true;
      }
   }

   void resetValues() {
      Iterator var2 = this.handlers.iterator();

      while(var2.hasNext()) {
         SettingsHandler handler = (SettingsHandler)var2.next();
         this.resetValue(handler);
      }

   }

   void resetValue(SettingsHandler handler) {
      String path = handler.getPath();
      if (this.global.isSaveable(path)) {
         String value = this.global.getDefault(path);
         this.log("Resetting:", handler.getClass().getSimpleName(), path, value);
         if (value != null) {
            this.log("Reset!");
            handler.setValue(value);
         }
      }
   }

   boolean canReset(SettingsHandler handler) {
      String key = handler.getPath();
      return this.global.isSaveable(key) && this.global.getDefault(handler.getPath()) != null;
   }

   void callPopup(MouseEvent e, SettingsHandler handler) {
      if (this.popup.isShowing()) {
         this.popup.setVisible(false);
      }

      this.defocus();
      int x = e.getX();
      int y = e.getY();
      this.selectedHandler = handler;
      this.infoItem.setVariables(handler.getPath());
      this.defaultItem.setEnabled(this.canReset(handler));
      this.popup.show((JComponent)e.getSource(), x, y);
   }

   public void block(Object reason) {
      Blocker.blockComponents((Container)this.container, (Object)reason);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents((Container)this.container, (Object)reason);
   }

   public void onLogin() throws LoginException {
      if (!this.checkValues()) {
         this.scene.setSettings(true);
         throw new LoginException("Invalid settings!");
      }
   }

   public void onLoginFailed() {
   }

   public void onLoginSuccess() {
   }

   protected void log(Object... o) {
      U.log("[SettingsPanel]", o);
   }
}
