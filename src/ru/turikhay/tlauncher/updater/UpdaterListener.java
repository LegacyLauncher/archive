package ru.turikhay.tlauncher.updater;

import ru.turikhay.tlauncher.updater.AdParser.AdMap;

public interface UpdaterListener {
	public void onUpdaterRequesting(Updater u);

	public void onUpdaterRequestError(Updater u);

	public void onUpdateFound(Update upd);

	public void onUpdaterNotFoundUpdate(Updater u);

	public void onAdFound(Updater u, AdMap adMap);
}
