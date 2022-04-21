package ru.turikhay.tlauncher.user;

public class MojangUserMigrationStatus {
    private final boolean canMigrate;
    private final Exception ex;

    public MojangUserMigrationStatus(boolean canMigrate, Exception ex) {
        this.canMigrate = canMigrate;
        this.ex = ex;
    }

    public MojangUserMigrationStatus(boolean canMigrate) {
        this(canMigrate, null);
    }

    public MojangUserMigrationStatus(Exception ex) {
        this(false, ex);
    }

    public boolean canMigrate() {
        return canMigrate;
    }

    public Exception getError() {
        return ex;
    }

    public Status asStatus() {
        if (ex != null) {
            return Status.ERROR;
        }
        return canMigrate ? Status.ELIGIBLE : Status.NOT_ELIGIBLE;
    }

    public enum Status {
        NONE,
        ERROR,
        NOT_ELIGIBLE,
        ELIGIBLE,
    }
}
