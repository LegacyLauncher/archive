package com.turikhay.tlauncher.ui;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.images.ImageCache;
import com.turikhay.tlauncher.ui.swing.ImagePanel;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;

public class ConnectionWarning extends ImagePanel implements UpdaterListener {
	private static final long serialVersionUID = 8089346864504410975L;

	private final String langPath;

	public ConnectionWarning() {
		super(ImageCache.getImage("warning.png"), 1.0F, 0.75F, false, false);

		this.langPath = "firewall";

		TLauncher.getInstance().getUpdater().addListener(this);
	}

	@Override
	protected boolean onClick() {
		if (!super.onClick())
			return false;

		Alert.showLocAsyncWarning(langPath);
		return true;
	}

	@Override
	public void onUpdaterRequestError(Updater u) {
		this.show();
	}

	@Override
	public void onUpdaterRequesting(Updater u) {
	}

	@Override
	public void onUpdateFound(Update upd) {
		this.hide();
	}

	@Override
	public void onUpdaterNotFoundUpdate(Updater u) {
		this.hide();
	}

	@Override
	public void onAdFound(Updater u, Ad ad) {
	}
}
