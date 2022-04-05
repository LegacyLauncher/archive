package ru.turikhay.tlauncher.bootstrap.exception;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileLockedException extends IOException {
    public static final long LOCK_COOLDOWN = 1000;

    public FileLockedException(String path, Throwable cause) {
        super(path, cause);
    }

    private static final Pattern pattern = Pattern.compile("(.+)(?: \\(.*\\))");

    public static void throwIfPresent(FileNotFoundException notFound) throws FileLockedException {
        FileLockedException ex = getIfPresent(notFound);
        if(ex != null) {
            throw ex;
        }
    }

    public static FileLockedException getIfPresent(FileNotFoundException notFound) {
        checkIfPresent: {
            String message = notFound.getMessage();
            if(message == null) {
                break checkIfPresent;
            }

            Matcher matcher = pattern.matcher(message);
            if(!matcher.matches()) {
                break checkIfPresent;
            }

            File file = new File(matcher.group(1));
            if(!file.isFile()) {
                break checkIfPresent;
            }

            return new FileLockedException(file.getAbsolutePath(), notFound);
        }
        return null;
    }
}
