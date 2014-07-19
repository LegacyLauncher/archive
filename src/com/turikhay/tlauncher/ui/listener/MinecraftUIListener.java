package com.turikhay.tlauncher.ui.listener;

import java.io.File;
import java.net.URI;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.minecraft.crash.Crash;
import com.turikhay.tlauncher.minecraft.crash.CrashSignatureContainer.CrashSignature;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftException;
import com.turikhay.tlauncher.minecraft.launcher.MinecraftListener;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.util.OS;
import com.turikhay.util.U;

public class MinecraftUIListener implements MinecraftListener {
	private final TLauncher t;
	private final LangConfiguration lang;

	public MinecraftUIListener(TLauncher tlauncher) {
		this.t = tlauncher;

		this.lang = t.getLang();
	}

	@Override
	public void onMinecraftPrepare() {
	}

	@Override
	public void onMinecraftAbort() {
	}

	@Override
	public void onMinecraftLaunch() {
		t.hide();
		t.getVersionManager().asyncRefresh();

		if (t.getUpdater() != null)
			t.getUpdater().asyncFindUpdate();
	}

	@Override
	public void onMinecraftClose() {
		if (!t.getLauncher().isLaunchAssist())
			return;

		t.show();

		if (t.getUpdater() != null)
			t.getUpdaterListener().applyDelayedUpdate();
	}

	@Override
	public void onMinecraftCrash(Crash crash) {
		String p = "crash.", title = Localizable.get(p + "title"), report = crash
				.getFile();

		if (!crash.isRecognized()) {
			Alert.showLocError(title, p + "unknown", null);
		} else {
			for (CrashSignature sign : crash.getSignatures()) {
				String path = sign.getPath(), message = p + path, url = message
						+ ".url";
				URI uri = U.makeURI(url);

				if (uri != null) {
					if (Alert.showLocQuestion(title, message, report))
						OS.openLink(uri);
				} else
					Alert.showLocMessage(title, message, report);
			}
		}

		if (report == null)
			return;

		if (Alert.showLocQuestion(p + "store")) {
			U.log("Removing crash report...");

			File file = new File(report);
			if (!file.exists())
				U.log("File is already removed. LOL.");
			else {

				if (!file.delete()) {
					U.log("Can't delete crash report file. Okay.");
					Alert.showLocMessage(p + "store.failed");
					return;
				}

				U.log("Yay, crash report file doesn't exist by now.");
			}
			Alert.showLocMessage(p + "store.success");
		}
	}

	@Override
	public void onMinecraftError(Throwable e) {
		Alert.showLocError("launcher.error.title", "launcher.error.unknown", e);
	}

	@Override
	public void onMinecraftKnownError(MinecraftException e) {
		Alert.showError(
				lang.get("launcher.error.title"),
				lang.get("launcher.error." + e.getLangPath(),
						(Object[]) e.getLangVars()), e);
	}

}
