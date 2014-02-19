package com.turikhay.tlauncher.minecraft.auth;

public class UserMigratedException extends AuthenticatorException {
	private static final long serialVersionUID = 7441756035466353515L;

	UserMigratedException() {
		super("This user has migrated", "migrated");
	}

}
