package ru.turikhay.tlauncher.minecraft.auth;

class UserMigratedException extends KnownAuthenticatorException {
    UserMigratedException() {
        super("This user has migrated", "migrated");
    }
}
