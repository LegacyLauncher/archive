package ru.turikhay.tlauncher.ui.settings;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.managers.VersionLists;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.converter.ActionOnLaunchConverter;
import ru.turikhay.tlauncher.ui.converter.ConnectionQualityConverter;
import ru.turikhay.tlauncher.ui.converter.ConsoleTypeConverter;
import ru.turikhay.tlauncher.ui.converter.DirectionConverter;
import ru.turikhay.tlauncher.ui.converter.LocaleConverter;
import ru.turikhay.tlauncher.ui.editor.EditorCheckBox;
import ru.turikhay.tlauncher.ui.editor.EditorComboBox;
import ru.turikhay.tlauncher.ui.editor.EditorFieldChangeListener;
import ru.turikhay.tlauncher.ui.editor.EditorFieldHandler;
import ru.turikhay.tlauncher.ui.editor.EditorFieldListener;
import ru.turikhay.tlauncher.ui.editor.EditorFileField;
import ru.turikhay.tlauncher.ui.editor.EditorGroupHandler;
import ru.turikhay.tlauncher.ui.editor.EditorHandler;
import ru.turikhay.tlauncher.ui.editor.EditorIntegerRangeField;
import ru.turikhay.tlauncher.ui.editor.EditorPair;
import ru.turikhay.tlauncher.ui.editor.EditorResolutionField;
import ru.turikhay.tlauncher.ui.editor.EditorTextField;
import ru.turikhay.tlauncher.ui.editor.TabbedEditorPanel;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.explorer.ImageFileExplorer;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.tlauncher.ui.login.LoginException;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.util.Direction;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.OS;
import ru.turikhay.util.Range;

public class SettingsPanel extends TabbedEditorPanel implements LoginForm.LoginProcessListener {
   private final DefaultScene scene;
   private final TabbedEditorPanel.EditorPanelTab minecraftTab;
   public final EditorFieldHandler directory;
   public final EditorFieldHandler resolution;
   public final EditorFieldHandler fullscreen;
   public final EditorFieldHandler javaArgs;
   public final EditorFieldHandler mcArgs;
   public final EditorFieldHandler javaPath;
   public final EditorFieldHandler memory;
   public final EditorGroupHandler versionHandler;
   private final TabbedEditorPanel.EditorPanelTab tlauncherTab;
   public final EditorFieldHandler launcherResolution;
   public final EditorFieldHandler background;
   public final EditorFieldHandler loginFormDirection;
   public final EditorFieldHandler autologinTimeout;
   public final EditorFieldHandler console;
   public final EditorFieldHandler fullCommand;
   public final EditorFieldHandler connQuality;
   public final EditorFieldHandler launchAction;
   public final EditorFieldHandler locale;
   private final TabbedEditorPanel.EditorPanelTab aboutTab;
   public final AboutPage about;
   private final BorderPanel buttonPanel;
   private final LocalizableButton saveButton;
   private final LocalizableButton defaultButton;
   private final ImageButton homeButton;
   private final JPopupMenu popup;
   private final LocalizableMenuItem infoItem;
   private final LocalizableMenuItem defaultItem;
   private EditorHandler selectedHandler;

   public SettingsPanel(DefaultScene sc) {
      super(tipTheme, new Insets(5, 10, 10, 10));
      if (this.tabPane.getExtendedUI() != null) {
         this.tabPane.getExtendedUI().setTheme(settingsTheme);
      }

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
      this.minecraftTab = new TabbedEditorPanel.EditorPanelTab("settings.tab.minecraft");
      this.directory = new EditorFieldHandler("minecraft.gamedir", new EditorFileField("settings.client.gamedir.prompt", new FileExplorer(1, true)), warning);
      this.directory.addListener(new EditorFieldChangeListener() {
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
      this.minecraftTab.add(new EditorPair("settings.client.gamedir.label", new EditorHandler[]{this.directory}));
      this.resolution = new EditorFieldHandler("minecraft.size", new EditorResolutionField("settings.client.resolution.width", "settings.client.resolution.height", this.global.getDefaultClientWindowSize(), false), restart);
      this.fullscreen = new EditorFieldHandler("minecraft.fullscreen", new EditorCheckBox("settings.client.resolution.fullscreen"));
      this.minecraftTab.add(new EditorPair("settings.client.resolution.label", new EditorHandler[]{this.resolution, this.fullscreen}));
      this.minecraftTab.nextPane();
      ReleaseType[] releaseTypes = ReleaseType.getDefinable();
      EditorFieldHandler[] versions = new EditorFieldHandler[releaseTypes.length];

      for(int i = 0; i < releaseTypes.length; ++i) {
         ReleaseType releaseType = releaseTypes[i];
         versions[i] = new EditorFieldHandler("minecraft.versions." + releaseType, new EditorCheckBox("settings.versions." + releaseType));
      }

      this.versionHandler = new EditorGroupHandler(versions);
      this.versionHandler.addListener(new EditorFieldChangeListener() {
         protected void onChange(String oldvalue, String newvalue) {
            TLauncher.getInstance().getVersionManager().updateVersionList();
         }
      });
      this.minecraftTab.add(new EditorPair("settings.versions.label", versions));
      this.minecraftTab.nextPane();
      this.javaArgs = new EditorFieldHandler("minecraft.javaargs", new EditorTextField("settings.java.args.jvm", true), warning);
      this.mcArgs = new EditorFieldHandler("minecraft.args", new EditorTextField("settings.java.args.minecraft", true), warning);
      this.minecraftTab.add(new EditorPair("settings.java.args.label", new EditorHandler[]{this.javaArgs, this.mcArgs}));
      final boolean isWindows = OS.WINDOWS.isCurrent();
      this.javaPath = new EditorFieldHandler("minecraft.javadir", new EditorFileField("settings.java.path.prompt", true, new FileExplorer(isWindows ? 0 : 1, true)) {
         public boolean isValueValid() {
            if (this.checkPath()) {
               return true;
            } else {
               Alert.showLocAsyncError("settings.java.path.doesnotexist");
               return false;
            }
         }

         private boolean checkPath() {
            if (!isWindows) {
               return true;
            } else {
               String path = this.getSettingsValue();
               if (path == null) {
                  return true;
               } else if (!path.endsWith(".exe")) {
                  return false;
               } else {
                  File javaDir = new File(path);
                  return javaDir.isFile();
               }
            }
         }
      }, warning);
      this.minecraftTab.add(new EditorPair("settings.java.path.label", new EditorHandler[]{this.javaPath}));
      this.minecraftTab.nextPane();
      this.memory = new EditorFieldHandler("minecraft.memory", new SettingsMemorySlider(), warning);
      this.minecraftTab.add(new EditorPair("settings.java.memory.label", new EditorHandler[]{this.memory}));
      this.add(this.minecraftTab);
      this.tlauncherTab = new TabbedEditorPanel.EditorPanelTab("settings.tab.tlauncher");
      this.launcherResolution = new EditorFieldHandler("gui.size", new EditorResolutionField("settings.client.resolution.width", "settings.client.resolution.height", this.global.getDefaultLauncherWindowSize(), true));
      this.launcherResolution.addListener(new EditorFieldListener() {
         protected void onChange(EditorHandler handler, String oldValue, String newValue) {
            if (SettingsPanel.this.tlauncher.isReady()) {
               IntegerArray arr = IntegerArray.parseIntegerArray(newValue);
               SettingsPanel.this.tlauncher.getFrame().setSize(arr.get(0), arr.get(1));
            }
         }
      });
      this.tlauncherTab.add(new EditorPair("settings.clientres.label", new EditorHandler[]{this.launcherResolution}));
      this.tlauncherTab.nextPane();
      this.loginFormDirection = new EditorFieldHandler("gui.direction.loginform", new EditorComboBox(new DirectionConverter(), Direction.values()));
      this.loginFormDirection.addListener(new EditorFieldChangeListener() {
         protected void onChange(String oldValue, String newValue) {
            if (SettingsPanel.this.tlauncher.isReady()) {
               SettingsPanel.this.tlauncher.getFrame().mp.defaultScene.updateDirection();
            }
         }
      });
      this.tlauncherTab.add(new EditorPair("settings.direction.label", new EditorHandler[]{this.loginFormDirection}));
      this.autologinTimeout = new EditorFieldHandler("login.auto.timeout", new EditorIntegerRangeField(new Range(2, 10)));
      this.tlauncherTab.add(new EditorPair("settings.tlauncher.autologin.label", new EditorHandler[]{this.autologinTimeout}));
      this.tlauncherTab.nextPane();
      this.background = new EditorFieldHandler("gui.background", new EditorFileField("settings.slide.list.prompt", true, new ImageFileExplorer()));
      this.background.addListener(new EditorFieldChangeListener() {
         protected void onChange(String oldValue, String newValue) {
            if (SettingsPanel.this.tlauncher.isReady()) {
               SettingsPanel.this.tlauncher.getFrame().mp.background.SLIDE_BACKGROUND.getThread().asyncRefreshSlide();
            }
         }
      });
      this.tlauncherTab.add(new EditorPair("settings.slide.list.label", new EditorHandler[]{this.background}));
      this.tlauncherTab.nextPane();
      this.console = new EditorFieldHandler("gui.console", new EditorComboBox(new ConsoleTypeConverter(), Configuration.ConsoleType.values()));
      this.console.addListener(new EditorFieldChangeListener() {
         // $FF: synthetic field
         private static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$configuration$Configuration$ConsoleType;

         protected void onChange(String oldvalue, String newvalue) {
            if (newvalue != null) {
               switch($SWITCH_TABLE$ru$turikhay$tlauncher$configuration$Configuration$ConsoleType()[Configuration.ConsoleType.get(newvalue).ordinal()]) {
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
         static int[] $SWITCH_TABLE$ru$turikhay$tlauncher$configuration$Configuration$ConsoleType() {
            int[] var10000 = $SWITCH_TABLE$ru$turikhay$tlauncher$configuration$Configuration$ConsoleType;
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

               $SWITCH_TABLE$ru$turikhay$tlauncher$configuration$Configuration$ConsoleType = var0;
               return var0;
            }
         }
      });
      this.tlauncherTab.add(new EditorPair("settings.console.label", new EditorHandler[]{this.console}));
      this.fullCommand = new EditorFieldHandler("gui.console.fullcommand", new EditorCheckBox("settings.console.fullcommand"));
      this.fullCommand.addListener(new EditorFieldChangeListener() {
         protected void onChange(String oldValue, String newValue) {
            if (SettingsPanel.this.tlauncher.isReady() && "true".equals(newValue)) {
               Alert.showLocAsyncWarning("settings.console.fullcommand.warning");
            }

         }
      });
      this.tlauncherTab.add(new EditorPair("settings.console.fullcommand.label", new EditorHandler[]{this.fullCommand}));
      this.tlauncherTab.nextPane();
      this.connQuality = new EditorFieldHandler("connection", new EditorComboBox(new ConnectionQualityConverter(), Configuration.ConnectionQuality.values()));
      this.connQuality.addListener(new EditorFieldChangeListener() {
         protected void onChange(String oldValue, String newValue) {
            SettingsPanel.this.tlauncher.getDownloader().setConfiguration(SettingsPanel.this.global.getConnectionQuality());
         }
      });
      this.tlauncherTab.add(new EditorPair("settings.connection.label", new EditorHandler[]{this.connQuality}));
      this.launchAction = new EditorFieldHandler("minecraft.onlaunch", new EditorComboBox(new ActionOnLaunchConverter(), Configuration.ActionOnLaunch.values()));
      this.tlauncherTab.add(new EditorPair("settings.launch-action.label", new EditorHandler[]{this.launchAction}));
      this.tlauncherTab.nextPane();
      this.locale = new EditorFieldHandler("locale", new EditorComboBox(new LocaleConverter(), this.global.getLocales()));
      this.locale.addListener(new EditorFieldChangeListener() {
         protected void onChange(String oldvalue, String newvalue) {
            if (SettingsPanel.this.tlauncher.getFrame() != null) {
               SettingsPanel.this.tlauncher.getFrame().updateLocales();
            }

         }
      });
      this.tlauncherTab.add(new EditorPair("settings.lang.label", new EditorHandler[]{this.locale}));
      this.add(this.tlauncherTab);
      this.aboutTab = new TabbedEditorPanel.EditorPanelTab("settings.tab.about");
      this.about = new AboutPage();
      this.aboutTab.add(this.about);
      this.add(this.aboutTab);
      this.saveButton = new LocalizableButton("settings.save");
      this.saveButton.setFont(this.saveButton.getFont().deriveFont(1));
      this.saveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsPanel.this.saveValues();
         }
      });
      this.defaultButton = new LocalizableButton("settings.default");
      this.defaultButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (Alert.showLocQuestion("settings.default.warning")) {
               SettingsPanel.this.resetValues();
            }

         }
      });
      this.homeButton = new ImageButton("home.png");
      this.homeButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SettingsPanel.this.updateValues();
            SettingsPanel.this.scene.setSidePanel((DefaultScene.SidePanel)null);
         }
      });
      Dimension size = this.homeButton.getPreferredSize();
      if (size != null) {
         this.homeButton.setPreferredSize(new Dimension(size.width * 2, size.height));
      }

      this.buttonPanel = new BorderPanel();
      this.buttonPanel.setCenter(sepPan(new Component[]{this.saveButton, this.defaultButton}));
      this.buttonPanel.setEast(uSepPan(new Component[]{this.homeButton}));
      this.tabPane.addChangeListener(new ChangeListener() {
         private final String aboutBlock = "abouttab";

         public void stateChanged(ChangeEvent e) {
            if (SettingsPanel.this.tabPane.getSelectedComponent() == SettingsPanel.this.aboutTab.getScroll()) {
               Blocker.blockComponents((Object)"abouttab", (Component[])(SettingsPanel.this.buttonPanel));
            } else {
               Blocker.unblockComponents((Object)"abouttab", (Component[])(SettingsPanel.this.buttonPanel));
            }

         }
      });
      this.container.setSouth(this.buttonPanel);
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
      Iterator var9 = this.handlers.iterator();

      while(var9.hasNext()) {
         final EditorHandler handler = (EditorHandler)var9.next();
         Component handlerComponent = handler.getComponent();
         handlerComponent.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
               if (e.getButton() == 3) {
                  SettingsPanel.this.callPopup(e, handler);
               }
            }
         });
      }

      this.updateValues();
   }

   void updateValues() {
      boolean globalUnSaveable = !this.global.isSaveable();
      Iterator var3 = this.handlers.iterator();

      while(true) {
         EditorHandler handler;
         String path;
         do {
            if (!var3.hasNext()) {
               return;
            }

            handler = (EditorHandler)var3.next();
            path = handler.getPath();
            String value = this.global.get(path);
            handler.updateValue(value);
            this.setValid(handler, true);
         } while(!globalUnSaveable && this.global.isSaveable(path));

         Blocker.block((Blockable)handler, (Object)"unsaveable");
      }
   }

   public boolean saveValues() {
      if (!this.checkValues()) {
         return false;
      } else {
         Iterator var2 = this.handlers.iterator();

         while(var2.hasNext()) {
            EditorHandler handler = (EditorHandler)var2.next();
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
         EditorHandler handler = (EditorHandler)var2.next();
         this.resetValue(handler);
      }

   }

   void resetValue(EditorHandler handler) {
      String path = handler.getPath();
      if (this.global.isSaveable(path)) {
         String value = this.global.getDefault(path);
         this.log(new Object[]{"Resetting:", handler.getClass().getSimpleName(), path, value});
         handler.setValue(value);
         this.log(new Object[]{"Reset!"});
      }
   }

   boolean canReset(EditorHandler handler) {
      String key = handler.getPath();
      return this.global.isSaveable(key) && this.global.getDefault(handler.getPath()) != null;
   }

   void callPopup(MouseEvent e, EditorHandler handler) {
      if (this.popup.isShowing()) {
         this.popup.setVisible(false);
      }

      this.defocus();
      int x = e.getX();
      int y = e.getY();
      this.selectedHandler = handler;
      this.updateResetMenu();
      this.infoItem.setVariables(handler.getPath());
      this.popup.show((JComponent)e.getSource(), x, y);
   }

   public void block(Object reason) {
      Blocker.blockComponents((Container)this.minecraftTab, (Object)reason);
      this.updateResetMenu();
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents((Container)this.minecraftTab, (Object)reason);
      this.updateResetMenu();
   }

   private void updateResetMenu() {
      if (this.selectedHandler != null) {
         this.defaultItem.setEnabled(!Blocker.isBlocked(this.selectedHandler));
      }

   }

   public void logginingIn() throws LoginException {
      if (!this.checkValues()) {
         this.scene.setSidePanel(DefaultScene.SidePanel.SETTINGS);
         throw new LoginException("Invalid settings!");
      }
   }

   public void loginFailed() {
   }

   public void loginSucceed() {
   }
}
