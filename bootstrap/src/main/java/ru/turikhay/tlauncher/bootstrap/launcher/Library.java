package ru.turikhay.tlauncher.bootstrap.launcher;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.json.ToStringBuildable;
import ru.turikhay.tlauncher.bootstrap.task.DownloadTask;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Library extends ToStringBuildable {
    private static final List<String> LIBRARY_REPO_LIST = Arrays.asList(U.shuffle(
            "https://u.tlauncher.ru/repo/libraries/",
            "https://tlauncherrepo.com/repo/libraries/",
            "https://tlaun.ch/repo/libraries/",
            "https://cdn.turikhay.ru/tlauncher/repo/libraries/"
    ));

    private String name, checksum;

    public String getName() {
        return name;
    }

    public File getFile(File folder) {
        return new File(U.requireNotNull(folder, "folder"), getPath());
    }

    public DownloadTask downloadTo(File dest) {
        return new DownloadTask(name, getUrlList(), dest, checksum);
    }

    public DownloadTask download(File folder) {
        return downloadTo(getFile(folder));
    }

    private List<URL> getUrlList() {
        ArrayList<URL> urlList = new ArrayList<URL>();
        String path = getPath();
        for(String prefix : LIBRARY_REPO_LIST) {
            urlList.add(U.toUrl(prefix + path));
        }
        return urlList;
    }

    private String getFilename() {
        return String.format("%s-%s.jar", getParts()[1], getParts()[2]);
    }

    private String getBaseDir() {
        return String.format("%s/%s/%s", StringUtils.replaceChars(getParts()[0], '.', '/'), getParts()[1], getParts()[2]);
    }

    public String getPath() {
        return String.format("%s/%s", getBaseDir(), getFilename());
    }

    @Expose
    private String[] parts;

    private String[] getParts() {
        if(parts == null) {
            parts = StringUtils.split(U.requireNotNull(name, "name"), ":", 3);
        }
        return parts;
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("name", name)
                .append("checksum", checksum);
    }
}
