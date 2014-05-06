package ru.turikhay.tlauncher.managers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.managers.ServerList.Server;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ServerListManager extends InterruptibleComponent {
	private final Gson gson;

	private final Repository repository;
	private ServerList serverList;

	private final List<ServerListManagerListener> listeners;

	private ServerListManager(ComponentManager manager, Repository repository)
			throws Exception {
		super(manager);

		if (repository == null)
			throw new NullPointerException("Repository cannot be NULL!");

		this.repository = repository;

		this.gson = TLauncher.getGson();
		this.listeners = Collections
				.synchronizedList(new ArrayList<ServerListManagerListener>());
	}

	public ServerListManager(ComponentManager manager) throws Exception {
		this(manager, Repository.SERVERLIST_REPO);
	}

	public ServerList getList() {
		return serverList;
	}

	@Override
	protected boolean refresh(int refreshID) {
		this.refreshList[refreshID] = true;

		log("Refreshing servers...");

		for (ServerListManagerListener listener : listeners)
			listener.onServersRefreshing(this);

		Object lock = new Object();
		Time.start(lock);

		ServerList result = null;
		Throwable e = null;

		try {
			result = loadFromList();
		} catch (Throwable e0) {
			e = e0;
		}

		if (isCancelled(refreshID)) {
			log("Server list refreshing has been cancelled (" + Time.stop(lock)
					+ " ms)");
			return false;
		}

		if (e != null) {
			for (ServerListManagerListener listener : listeners)
				listener.onServersRefreshingFailed(this);

			log("Cannot refresh servers (" + Time.stop(lock) + " ms)", e);
			return true;
		}

		if (result != null)
			this.serverList = result;

		log("Servers has been refreshed (" + Time.stop(lock) + " ms)");
		log(serverList);

		this.refreshList[refreshID] = false;

		for (ServerListManagerListener listener : listeners)
			listener.onServersRefreshed(this);

		return true;
	}

	public boolean reconstructList(String version, File listFile)
			throws IOException {
		log("Reconstructing server list (servers.dat)...");

		if (version == null)
			throw new NullPointerException("Version cannot be NULL!");

		if (listFile == null)
			throw new NullPointerException("File cannot be NULL!");

		if (serverList == null) {
			log("Promoted server list is NULL. Server list won't be reconstructed.");
			return false;
		}

		if (serverList.isEmpty()) {
			log("Promoted server list is empty. Server list won't be reconstructed.");
			return false;
		}

		// List containing promoted servers compaible with running version.
		ServerList list = new ServerList();

		for (Server prefServer : serverList.getList())
			if (version.matches(prefServer.getVersion()))
				list.add(prefServer);

		ServerList userList = ServerList.loadFromFile(listFile);

		// Remove all promoted servers from user list to avoid dublicates and
		// some other shi~
		for (Server prefServer : serverList.getList())
			if (userList.contains(prefServer))
				userList.remove(prefServer);

		// Add into filtered promoted server list filtered user server list.
		ServerList resultList = ServerList.sortLists(list, userList);

		resultList.save(listFile);

		return true;
	}

	private ServerList loadFromList() throws JsonSyntaxException, IOException {
		Object lock = new Object();
		Time.start(lock);

		ServerList list = gson.fromJson(repository.getUrl(), ServerList.class);

		log("Got in", Time.stop(lock), "ms");
		return list;
	}

	@Override
	protected void log(Object... w) {
		U.log("[" + getClass().getSimpleName() + "]", w);
	}
}
