package ru.turikhay.tlauncher.ui.listener;

import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.updater.AdParser;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;

public class RequiredUpdateListener implements UpdaterListener {

	public RequiredUpdateListener(Updater updater) {
		updater.addListener(this);
	}

	@Override
	public void onUpdaterRequesting(Updater u) {
	}

	@Override
	public void onUpdaterRequestError(Updater u) {
	}

	@Override
	public void onUpdateFound(Update upd) {
		if(!upd.isRequired()) return;

		String
		prefix = "updater.required.found.",
		title = prefix + "title",
		message  = prefix + "message";

		Alert.showWarning(Localizable.get(title), Localizable.get(message, upd.getVersion() +" ("+ upd.getCode() +")"), upd.getDescription());

		UpdateUIListener listener = new UpdateUIListener(upd);
		listener.push();
	}

	@Override
	public void onUpdaterNotFoundUpdate(Updater u) {
	}

	@Override
	public void onAdFound(Updater u, AdParser ad) {
	}

}
