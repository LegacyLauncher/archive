package ru.turikhay.tlauncher.ui.explorer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.util.OS;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FileExplorer extends JFileChooser {
    private static final Logger LOGGER = LogManager.getLogger(FileExplorer.class);

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
            throw new Exception("couldn't create explorer", var1);
        }
    }

    public static String normalize(String path) {
        if (!OS.WINDOWS.isCurrent()
                || StringUtils.isEmpty(path)
                || !Character.isWhitespace(path.charAt(path.length() - 1))
        ) {
            return path;
        }
        LOGGER.debug("Stripping whitespace chars: \"{}\"", path);
        return StringUtils.stripEnd(path, null);
    }
}
