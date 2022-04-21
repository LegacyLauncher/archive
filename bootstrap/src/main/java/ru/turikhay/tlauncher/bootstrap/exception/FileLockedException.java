package ru.turikhay.tlauncher.bootstrap.exception;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLockedException extends IOException {
    public static final long LOCK_COOLDOWN = 100;

    public FileLockedException(String path, Throwable cause) {
        super(path, cause);
    }

    private static final Pattern pattern = Pattern.compile("(.+) \\(.*\\)");

    public static FileLockedException getIfPresent(FileNotFoundException notFound) {
        String message = notFound.getMessage();
        if (message == null) {
            return null;
        }

        Matcher matcher = pattern.matcher(message);
        if (!matcher.matches()) {
            return null;
        }

        File file = new File(matcher.group(1));
        if (!file.isFile()) {
            return null;
        }

        return new FileLockedException(file.getAbsolutePath(), notFound);
    }
}
