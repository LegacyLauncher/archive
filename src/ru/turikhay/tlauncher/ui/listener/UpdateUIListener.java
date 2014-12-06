package ru.turikhay.tlauncher.ui.listener;

import java.net.URI;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.UpdateListener;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.util.OS;

public class UpdateUIListener implements UpdateListener {

	private final TLauncher t;
	private final Update u;

	public UpdateUIListener(Update u) {
		if(u == null)
			throw new NullPointerException();

		this.t = TLauncher.getInstance();
		this.u = u;

		u.addListener(this);
	}

	public void push() {
		if (Updater.isAutomode()) {
			block();
			u.download(true);
		}
		else
			openUpdateLink( u.getDownloadLink() );
	}

	@Override
	public void onUpdateError(Update u, Throwable e) {
		if (Alert.showLocQuestion("updater.error.title", "updater.download-error", e))
			openUpdateLink(u.getDownloadLink());

		unblock();
	}

	@Override
	public void onUpdateDownloading(Update u) {
	}

	@Override
	public void onUpdateDownloadError(Update u, Throwable e) {
		this.onUpdateError(u, e);
	}

	@Override
	public void onUpdateReady(Update u) {
		onUpdateReady(u, false, false);
	}

	private static void onUpdateReady(Update u, boolean force, boolean showChangeLog) {
		Alert.showLocWarning("updater.downloaded", showChangeLog? u.getDescription() : null);
		u.apply();
	}

	@Override
	public void onUpdateApplying(Update u) {
	}

	@Override
	public void onUpdateApplyError(Update u, Throwable e) {
		if (Alert.showLocQuestion("updater.save-error", e))
			openUpdateLink(u.getDownloadLink());

		unblock();
	}

	private static boolean openUpdateLink(URI uri) {
		if(OS.openLink(uri, false))
			return true;

		Alert.showLocError("updater.found.cannotopen", uri);
		return false;
	}

	private void block() {
		Blocker.block(t.getFrame().mp, "updater");
	}

	private void unblock() {
		Blocker.unblock(t.getFrame().mp, "updater");
	}
}
