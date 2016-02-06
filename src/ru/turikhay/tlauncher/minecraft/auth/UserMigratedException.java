package ru.turikhay.tlauncher.minecraft.auth;

class UserMigratedException extends AuthenticatorException {
   UserMigratedException() {
      super("This user has migrated", "migrated");
   }
}
