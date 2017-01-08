package net.minecraft.launcher.updater;

import com.google.gson.JsonSyntaxException;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Version;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class LocalVersionList extends StreamVersionList {
    private FileExplorer explorer;
    private JFrame parent;
    private File baseDirectory;
    private File baseVersionsDir;

    public LocalVersionList() throws IOException {
        setBaseDirectory(MinecraftUtil.getWorkingDirectory());
    }

    public File getBaseDirectory() {
        return baseDirectory;
    }

    public File getVersionsDirectory() {
        return baseVersionsDir;
    }

    public void setBaseDirectory(File directory) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("Base directory is NULL!");
        } else if (!directory.isDirectory()) {
            throw new IOException("Directory is not yet created!");
        } else if (!directory.canWrite()) {
            throw new IOException("Directory is not accessible!");
        } else {
            baseDirectory = directory;
            baseVersionsDir = new File(baseDirectory, "versions");
        }
    }

    public void refreshVersions() throws IOException {
        clearCache();
        File[] files = baseVersionsDir.listFiles();
        if (files != null) {
            File[] var5 = files;
            int var4 = files.length;

            for (int var3 = 0; var3 < var4; ++var3) {
                File directory = var5[var3];
                String id = directory.getName();
                File jsonFile = new File(directory, id + ".json");
                if (directory.isDirectory() && jsonFile.isFile()) {
                    try {
                        CompleteVersion ex = gson.fromJson(getUrl("versions/" + id + "/" + id + ".json"), CompleteVersion.class);
                        if (ex == null) {
                            log("JSON descriptor of version \"" + id + "\" in NULL, it won\'t be added in list as local.");
                        } else {
                            ex.setID(id);
                            ex.setSource(Repository.LOCAL_VERSION_REPO);
                            ex.setVersionList(this);
                            addVersion(ex);
                        }
                    } catch (Exception var9) {
                        log("Error occurred while parsing local version", id, var9);
                    }
                }
            }

        }
    }

    public void saveVersion(CompleteVersion version) throws IOException {
        String text = serializeVersion(version);
        File target = new File(baseVersionsDir, version.getID() + "/" + version.getID() + ".json");
        FileUtil.writeFile(target, text);
    }

    public void deleteVersion(String id, boolean deleteLibraries) throws IOException {
        CompleteVersion version = getCompleteVersion(id);
        if (version == null) {
            throw new IllegalArgumentException("Version is not installed!");
        } else {
            File dir = new File(baseVersionsDir, id + '/');
            if (!dir.isDirectory()) {
                throw new IOException("Cannot find directory: " + dir.getAbsolutePath());
            } else {
                FileUtil.deleteDirectory(dir);
                if (deleteLibraries) {
                    Iterator var6 = version.getClassPath(baseDirectory).iterator();

                    while (var6.hasNext()) {
                        File nativeLib = (File) var6.next();
                        FileUtil.deleteFile(nativeLib);
                    }

                    var6 = version.getNatives().iterator();

                    while (var6.hasNext()) {
                        String nativeLib1 = (String) var6.next();
                        FileUtil.deleteFile(new File(baseDirectory, nativeLib1));
                    }

                }
            }
        }
    }

    protected InputStream getInputStream(String uri) throws IOException {
        return new FileInputStream(new File(baseDirectory, uri));
    }

    public boolean hasAllFiles(CompleteVersion version, OS os) {
        Set files = version.getRequiredFiles(os);
        Iterator var5 = files.iterator();

        File required;
        do {
            if (!var5.hasNext()) {
                return true;
            }

            String file = (String) var5.next();
            required = new File(baseDirectory, file);
        } while (required.isFile() && required.length() != 0L);

        return false;
    }

    public CompleteVersion getCompleteVersion(Version version) throws JsonSyntaxException, IOException {
        if (version instanceof CompleteVersion) {
            return (CompleteVersion) version;
        } else if (version == null) {
            throw new NullPointerException("Version cannot be NULL!");
        } else {
            CompleteVersion complete = gson.fromJson(getUrl("versions/" + version.getID() + "/" + version.getID() + ".json"), CompleteVersion.class);
            complete.setID(version.getID());
            complete.setVersionList(this);
            Collections.replaceAll(versions, version, complete);
            return complete;
        }
    }
}
