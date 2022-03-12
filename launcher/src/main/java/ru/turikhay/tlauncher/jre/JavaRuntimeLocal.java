package ru.turikhay.tlauncher.jre;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public class JavaRuntimeLocal implements JavaRuntime {
    private final String name;
    private final String platform;
    private final File directory;

    public JavaRuntimeLocal(String name, String platform, File directory) {
        this.name = Objects.requireNonNull(name);
        this.platform = Objects.requireNonNull(platform);
        this.directory = Objects.requireNonNull(directory);
    }

    public String getVersion() throws IOException {
        return FileUtils.readFileToString(new File(directory, ".version"), StandardCharsets.UTF_8);
    }

    public void writeVersion(String version) throws IOException {
        FileUtils.writeStringToFile(new File(directory, ".version"), version, StandardCharsets.UTF_8);
    }

    public boolean hasOverride() {
        if (!directory.isDirectory()) {
            return false;
        }
        File[] files = directory.listFiles();
        if (files == null) {
            return false;
        }
        return Arrays.stream(files).anyMatch(file -> file.getName().startsWith("override"));
    }

    /*public JavaRuntimeLocalIntegrityInfo getIntegrityInfo() throws IOException {
        return JavaRuntimeLocalIntegrityInfo.parse(new File(directory, name + ".sha1"));
    }

    public void writeIntegrityInfo(JavaRuntimeLocalIntegrityInfo info) throws IOException {
        info.write(new File(directory, name + ".sha1"));
    }*/

    public File getDirectory() {
        return directory;
    }

    public File getWorkingDirectory() {
        return new File(directory, name);
    }

    public File getExecutableFile() {
        Objects.requireNonNull(platform, "platform");
        String path;

        switch (JavaPlatform.getOSByPlatform(platform)) {
            case LINUX:
                path = "bin/java";
                break;
            case WINDOWS:
                path = "bin\\javaw.exe";
                break;
            case OSX:
                path = "jre.bundle/Contents/Home/bin/java";
                break;
            default:
                throw new RuntimeException("unsupported OS");
        }

        return new File(getWorkingDirectory(), path);
    }

    /*public List<FileIntegrityEntry> check() throws IOException {
        return getIntegrityInfo().checkEntriesResolving(getWorkingDirectory());
    }*/

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPlatform() {
        return platform;
    }
}
