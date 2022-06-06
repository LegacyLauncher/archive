package ru.turikhay.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class MinecraftUtil {
    private static final Logger LOGGER = LogManager.getLogger(MinecraftUtil.class);

    private static FileExplorer explorer;
    private static JFrame parent;

    private static boolean checkDirectory(File directory) {
        return directory.isDirectory() && directory.canRead() && directory.canWrite();
    }

    private static File chooseWorkingDir(String path) {
        File directory = new File(path);

        if (checkDirectory(directory)) return directory;
        try {
            FileUtil.createFolder(directory);
            if (checkDirectory(directory)) return directory;
        } catch (IOException e) {
            LOGGER.warn("Got error trying to create directory", e);
        }

        // so we can't create directory
        // let's get default directory
        File defaultDirectory = getDefaultWorkingDirectory();
        File preferredDirectory;
        // let's create it
        try {
            FileUtil.createFolder(defaultDirectory);
            preferredDirectory = defaultDirectory;
        } catch (IOException e) {
            LOGGER.warn("Can't even create default folder", e);
            preferredDirectory = new File(System.getProperty("user.home", "/"));
        }

        do {
            LOGGER.info("Current directory cannot be written to: {}", directory);
            Alert.showLocError("version.dir.noaccess", directory);
            directory = showExplorer(preferredDirectory);
            if (directory == null) {
                LOGGER.info("No directory selected, killing launcher. Good bye!");
                System.exit(0);
            }
            LOGGER.info("User selected directory: {}", directory);
        } while (!checkDirectory(directory));

        // User asked for "please remove default directory if it is empty and not used for launcher"
        if (defaultDirectory != directory && defaultDirectory.isDirectory() && defaultDirectory.length() == 0) {
            LOGGER.info("Default game folder is exists, is empty and is not used. Deleting");
            defaultDirectory.delete();
        }


        return directory;
    }

    public static File getWorkingDirectory(boolean choose) {
        File defaultDirectory = getDefaultWorkingDirectory();

        if (TLauncher.getInstance() == null) {
            return defaultDirectory;
        }

        String path = TLauncher.getInstance().getSettings().get("minecraft.gamedir");

        if (path == null) {
            return defaultDirectory;
        }

        return choose ? chooseWorkingDir(path) : new File(path);
    }

    public static File getWorkingDirectory() {
        return getWorkingDirectory(true);
    }

    private static File showExplorer(File preferred) {
        if (explorer == null) {
            parent = new JFrame();

            try {
                explorer = FileExplorer.newExplorer();
                explorer.setSelectedFile(preferred);
                explorer.setFileSelectionMode(1);
            } catch (Exception e) {
                String answer = Alert.showLocInputQuestion("version.dir.noexplorer");
                if (answer == null) {
                    return null;
                }

                return new File(answer);
            }
        }

        return explorer.showDialog(parent) != 0 ? null : explorer.getSelectedFile();
    }

    public static File getSystemRelatedFile(String path) {
        String userHome = System.getProperty("user.home", ".");
        File file;
        switch (OS.CURRENT) {
            case WINDOWS:
                String applicationData = System.getenv("APPDATA");
                String folder = applicationData != null ? applicationData : userHome;
                file = new File(folder, path);
                break;
            case OSX:
                file = new File(userHome, "Library/Application Support/" + path);
                break;
            case LINUX:
            default:
                file = new File(userHome, path);
        }

        return file;
    }

    public static File getSystemRelatedDirectory(String path, boolean hide) {
        if (hide && !OS.is(OS.OSX, OS.UNKNOWN)) {
            path = '.' + path;
        }

        return getSystemRelatedFile(path);
    }

    public static File getSystemRelatedDirectory(String path) {
        return getSystemRelatedDirectory(path, true);
    }

    public static File getDefaultWorkingDirectory() {
        return getSystemRelatedDirectory(TLauncher.getFolder());
    }

    public static File getOptionsFile() {
        return getFile("options.txt");
    }

    private static File getFile(String name) {
        return new File(getWorkingDirectory(), name);
    }
}
