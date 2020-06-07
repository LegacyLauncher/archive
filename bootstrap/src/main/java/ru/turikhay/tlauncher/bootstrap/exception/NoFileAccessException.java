package ru.turikhay.tlauncher.bootstrap.exception;

import java.io.File;
import java.io.IOException;

public class NoFileAccessException extends IOException {
    private final File file;
    private final AccessScope scope;

    public NoFileAccessException(File file, AccessScope scope) {
        super(file == null? null : file.getAbsolutePath());
        this.file = file;
        this.scope = scope;
    }

    public File getFile() {
        return file;
    }

    public AccessScope getScope() {
        return scope;
    }

    public static void throwIfNoAccess(File file) throws NoFileAccessException {
        if(file == null) {
            throw new NullPointerException();
        }
        if (!AccessScope.READ.check(file)) throw new NoFileAccessException(file, AccessScope.READ);
    }

    public enum AccessScope {
        READ {
            @Override
            public boolean check(File file) {
                return file.canRead();
            }
        },
        WRITE {
            @Override
            public boolean check(File file) {
                return file.canWrite();
            }
        };

        public abstract boolean check(File file);
    }
}
