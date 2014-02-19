package com.turikhay.tlauncher.minecraft;

public class FakeCrashSignature extends CrashSignature {

	FakeCrashSignature(int exitcode, String pattern, String name) {
		super(exitcode, pattern, name, null);
	}

}
