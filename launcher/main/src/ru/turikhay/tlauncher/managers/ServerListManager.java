package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.Time;
import ru.turikhay.util.U;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ServerListManager extends InterruptibleComponent {
    private final Gson gson;
    private final Repository repository;
    private ServerList serverList;
    private final List<ServerListManagerListener> listeners;
    private static final String logPrefix = '[' + ServerListManager.class.getSimpleName() + ']';

    private ServerListManager(ComponentManager manager, Repository repository) throws Exception {
        super(manager);
        if (repository == null) {
            throw new NullPointerException("Repository cannot be NULL!");
        } else {
            this.repository = repository;
            gson = U.getGson();
            listeners = Collections.synchronizedList(new ArrayList());
        }
    }

    public ServerListManager(ComponentManager manager) throws Exception {
        this(manager, Repository.SERVERLIST_REPO);
    }

    public ServerList getList() {
        return serverList;
    }

    protected boolean refresh(int refreshID) {
        refreshList[refreshID] = true;
        log("Refreshing servers...");
        Iterator result = listeners.iterator();

        while (result.hasNext()) {
            ServerListManagerListener lock = (ServerListManagerListener) result.next();
            lock.onServersRefreshing(this);
        }

        Object lock1 = new Object();
        Time.start(lock1);
        ServerList result1 = null;
        Throwable e = null;

        try {
            result1 = loadFromList();
        } catch (Throwable var7) {
            e = var7;
        }

        if (isCancelled(refreshID)) {
            log("Server list refreshing has been cancelled (" + Time.stop(lock1) + " ms)");
            return false;
        } else {
            ServerListManagerListener listener;
            Iterator var6;
            if (e != null) {
                var6 = listeners.iterator();

                while (var6.hasNext()) {
                    listener = (ServerListManagerListener) var6.next();
                    listener.onServersRefreshingFailed(this);
                }

                log("Cannot refresh servers (" + Time.stop(lock1) + " ms)", e);
                return true;
            } else {
                if (result1 != null) {
                    serverList = result1;
                }

                log("Servers has been refreshed (" + Time.stop(lock1) + " ms)");
                log(serverList);
                refreshList[refreshID] = false;
                var6 = listeners.iterator();

                while (var6.hasNext()) {
                    listener = (ServerListManagerListener) var6.next();
                    listener.onServersRefreshed(this);
                }

                return true;
            }
        }
    }

    public static boolean reconstructList(ServerList promoted, File serversDat) throws IOException {
        if (promoted == null) {
            throw new NullPointerException("list");
        } else if (serversDat == null) {
            throw new NullPointerException("servers.dat file");
        } else {
            slog("Reconstructing...");
            slog("Loading from file...");

            boolean exist = serversDat.isFile();
            ServerList userList = exist ? ServerList.loadFromFile(serversDat) : new ServerList();
            Iterator var4 = promoted.getList().iterator();

            while (var4.hasNext()) {
                ServerList.Server resultList = (ServerList.Server) var4.next();
                if (userList.contains(resultList)) {
                    userList.remove(resultList);
                }
            }

            ServerList resultList1 = ServerList.sortLists(promoted, userList);

            resultList1.save(serversDat);
            return true;
        }
    }

    private ServerList loadFromList() throws JsonSyntaxException, IOException {
        Object lock = new Object();
        Time.start(lock);
        ServerList list = gson.fromJson(repository.read(""), ServerList.class);
        log("Got in", Long.valueOf(Time.stop(lock)), "ms");
        return list;
    }

    protected void log(Object... w) {
        U.log("[" + getClass().getSimpleName() + "]", w);
    }

    private static void slog(Object... o) {
        U.log(logPrefix, o);
    }
}
