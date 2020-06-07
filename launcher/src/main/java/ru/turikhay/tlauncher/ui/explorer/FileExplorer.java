package ru.turikhay.tlauncher.ui.explorer;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileExplorer extends JFileChooser {
    public void setCurrentDirectory(File dir) {
        if (dir == null) {
            dir = getFileSystemView().getDefaultDirectory();
        }

        super.setCurrentDirectory(dir);
    }

    public void setCurrentDirectory(String sDir) {
        File dir = sDir == null ? null : new File(sDir);
        setCurrentDirectory(dir);
    }

    public int showDialog(Component parent) {
        return showDialog(parent, UIManager.getString("FileChooser.directoryOpenButtonText"));
    }

    public File[] getSelectedFiles() {
        File[] selectedFiles = super.getSelectedFiles();
        if (selectedFiles.length > 0) {
            return selectedFiles;
        } else {
            File selectedFile = super.getSelectedFile();
            return selectedFile == null ? null : new File[]{selectedFile};
        }
    }

    public static FileExplorer newExplorer() throws Exception {
        try {
            return new FileExplorer();
        } catch (Throwable var1) {
            throw new Exception("couldn\'t create explorer");
        }
    }
}
