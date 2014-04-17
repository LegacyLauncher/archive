package com.turikhay.tlauncher.minecraft.crash;

import java.util.Scanner;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.turikhay.tlauncher.minecraft.crash.CrashSignatureContainer.CrashSignature;
import com.turikhay.tlauncher.minecraft.crash.CrashSignatureContainer.CrashSignatureContainerDeserializer;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;

public class CrashDescriptor {
	private static CrashSignatureContainer container;

	public final static int universalExitCode = 0;
	private final static String loggerPrefix = "[Crash]";

	private final MinecraftLauncher launcher;

	public CrashDescriptor(MinecraftLauncher launcher) {
		if (launcher == null)
			throw new NullPointerException();

		this.launcher = launcher;
	}

	public Crash scan() {
		int exitCode = launcher.getExitCode();
		if (exitCode == universalExitCode)
			return null; // No crash, everything seems to be OK.

		Crash crash = new Crash();

		Pattern startPattern = container.getPattern("start_crash"), filePattern = container
				.getPattern("crash");

		String version = launcher.getVersion();

		Scanner scanner = new Scanner(launcher.getStream().getOutput());
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if (startPattern.matcher(line).matches()) {
				log("Found start of crash report, will not scan further.");
				break;
			}

			if (filePattern.matcher(line).matches()) {
				String file = filePattern.matcher(line).group(1);
				crash.setFile(file);

				log("Found crash report file:", file);
				continue;
			}

			for (CrashSignature signature : container.getSignatures()) {
				if (signature.hasVersion()
						&& !signature.getVersion().matcher(version).matches())
					continue; // Version-specific signature

				if (signature.getExitCode() != universalExitCode
						&& signature.getExitCode() != exitCode)
					continue; // Exit code specified signature

				if (signature.hasPattern()
						&& !signature.getPattern().matcher(line).matches())
					continue; // Doesn't match, skip.

				if (signature.isFake()) {
					log("Minecraft closed with an illegal exit code not due to error. Scanning has been cancelled");
					log("Fake signature:", signature.getName());

					scanner.close();
					return null;
				}

				if (crash.hasSignature(signature))
					continue; // Already have, skip.

				log("Signature \"" + signature.getName() + "\" matches!");
				crash.addSignature(signature);
			}
		}
		scanner.close();

		if (crash.isRecognized())
			log("Crash has been recognized!");

		return crash;
	}

	void log(Object... w) {
		launcher.getLogger().log(loggerPrefix, w);
		U.log(loggerPrefix, w);
	}

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(CrashSignatureContainer.class,
				new CrashSignatureContainerDeserializer());

		Gson gson = builder.create();

		try {
			container = gson.fromJson(FileUtil
					.getResource(CrashDescriptor.class
							.getResource("signatures.json")),
					CrashSignatureContainer.class);
		} catch (Exception e) {
			U.log("Cannot parse crash signatures!", e);
			container = new CrashSignatureContainer();
		}
	}

}
