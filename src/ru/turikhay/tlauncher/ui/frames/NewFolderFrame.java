package ru.turikhay.tlauncher.ui.frames;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.editor.EditorFileField;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class NewFolderFrame extends VActionFrame {
   private final TLauncher t;

   public NewFolderFrame(TLauncher t, File file) {
      super(SwingUtil.magnify(500));
      this.t = t;
      this.setDefaultCloseOperation(2);
      this.setTitlePath("newfolder.title", new Object[0]);
      this.getHead().setText("newfolder.head");
      this.getBodyText().setText("newfolder.body");

      FileExplorer dirExplorer;
      try {
         dirExplorer = FileExplorer.newExplorer();
         dirExplorer.setFileSelectionMode(1);
         dirExplorer.setFileHidingEnabled(false);
      } catch (Exception var9) {
         dirExplorer = null;
      }

      final EditorFileField fileField = new EditorFileField("newfolder.select.prompt", "newfolder.select.browse", dirExplorer, false, false);
      fileField.setSettingsValue(file.getAbsolutePath());
      ExtendedPanel fileFieldShell = new ExtendedPanel();
      fileFieldShell.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.fill = 2;
      c.weightx = 1.0D;
      c.gridx = 0;
      c.gridy = 0;
      fileFieldShell.add(new LocalizableLabel("newfolder.select.title"), c);
      ++c.gridy;
      fileFieldShell.add(fileField, c);
      this.getBody().add(fileFieldShell);
      this.getFooter().setLayout(new GridBagLayout());
      c = new GridBagConstraints();
      c.gridx = -1;
      ++c.gridx;
      c.weightx = 0.0D;
      c.fill = 3;
      LocalizableButton cancelButton = new LocalizableButton("newfolder.button.cancel");
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            NewFolderFrame.this.dispose();
         }
      });
      this.getFooter().add(cancelButton, c);
      ++c.gridx;
      c.weightx = 1.0D;
      c.fill = 2;
      this.getFooter().add(new ExtendedPanel(), c);
      ++c.gridx;
      c.weightx = 0.0D;
      c.fill = 3;
      final LocalizableButton okButton = new LocalizableButton("newfolder.button.ok");
      okButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            NewFolderFrame.this.changeFolder(fileField.isValueValid() ? fileField.getSelectedFile() : null);
         }
      });
      this.getFooter().add(okButton, c);
      this.addWindowListener(new WindowAdapter() {
         public void windowActivated(WindowEvent e) {
            okButton.requestFocus();
         }
      });
      this.pack();
   }

   private void changeFolder(File folder) {
      if (folder == null) {
         Alert.showLocError("newfolder.select.error");
      } else {
         File currentFolder = MinecraftUtil.getWorkingDirectory();
         if (folder.getAbsolutePath().startsWith(currentFolder.getAbsolutePath())) {
            Alert.showLocError("newfolder.select.error.title", "newfolder.select.error.inside", (Object)null);
         } else {
            this.t.getSettings().set("minecraft.gamedir", folder.getAbsolutePath());
            U.log("Changed folder with NewFolderFrame:", folder);
            this.dispose();
         }
      }
   }

   public static boolean shouldWeMoveFrom(File currentDir) {
      if (currentDir == null) {
         return true;
      } else if (!currentDir.isDirectory()) {
         try {
            FileUtil.createFolder(currentDir);
            return false;
         } catch (Exception var3) {
            U.log(currentDir, "is not accessible");
            currentDir.delete();
            return true;
         }
      } else if (currentDir.canRead() && currentDir.canWrite() && currentDir.canExecute()) {
         File[] list = currentDir.listFiles();
         if (list == null) {
            U.log(currentDir, "has null listing?!");
            return true;
         } else if (list.length == 0) {
            return false;
         } else {
            File profileFile = new File(currentDir, "tlauncher_profiles.json");
            U.log(currentDir, "has profile file:", profileFile.isFile());
            return !profileFile.isFile();
         }
      } else {
         U.log(currentDir, "is not readable/writable/executable");
         return true;
      }
   }

   public static File selectDestination() {
      ArrayList suggestions = new ArrayList();
      suggestions.addAll(Arrays.asList(MinecraftUtil.getSystemRelatedDirectory("minecraft")));
      if (OS.WINDOWS.isCurrent()) {
         suggestions.addAll(Arrays.asList(new File("D:\\Games\\Minecraft"), new File("C:\\Games\\Minecraft")));
      }

      suggestions.addAll(Arrays.asList(MinecraftUtil.getSystemRelatedDirectory("Minecraft", false), MinecraftUtil.getSystemRelatedDirectory("tlauncher/" + TLauncher.getBrand())));
      Iterator var1 = suggestions.iterator();

      File suggestion;
      do {
         if (!var1.hasNext()) {
            return null;
         }

         suggestion = (File)var1.next();
      } while(shouldWeMoveFrom(suggestion));

      return suggestion;
   }
}
