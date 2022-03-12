package ru.turikhay.tlauncher.ui.versions;

import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionSyncInfo;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.managers.SwingVersionManagerListener;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.scenes.VersionManagerScene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VersionHandler implements Blockable, VersionHandlerListener {
    public static final String REFRESH_BLOCK = "refresh";
    public static final String SINGLE_SELECTION_BLOCK = "single-select";
    public static final String START_DOWNLOAD = "start-download";
    public static final String STOP_DOWNLOAD = "stop-download";
    public static final String DELETE_BLOCK = "deleting";
    private final List<VersionHandlerListener> listeners;
    private final VersionHandler instance = this;
    public final VersionManagerScene scene;
    final VersionHandlerThread thread;
    public final VersionList list;
    final VersionManager vm;
    final Downloader downloader;
    List<VersionSyncInfo> selected;
    List<VersionSyncInfo> downloading;
    VersionFilter filter;

    public VersionHandler(VersionManagerScene scene) {
        this.scene = scene;
        listeners = Collections.synchronizedList(new ArrayList<>());
        downloading = Collections.synchronizedList(new ArrayList<>());
        TLauncher launcher = TLauncher.getInstance();
        vm = launcher.getVersionManager();
        downloader = launcher.getDownloader();
        list = new VersionList(this);
        thread = new VersionHandlerThread(this);
        vm.addListener(new SwingVersionManagerListener(new VersionManagerListener() {
            public void onVersionsRefreshing(VersionManager manager) {
                instance.onVersionRefreshing(manager);
            }

            public void onVersionsRefreshed(VersionManager manager) {
                instance.onVersionRefreshed(manager);
            }

            public void onVersionsRefreshingFailed(VersionManager manager) {
                onVersionsRefreshed(manager);
            }
        }));
        onVersionDeselected();
    }

    void addListener(VersionHandlerListener listener) {
        listeners.add(listener);
    }

    void update() {
        if (selected != null) {
            onVersionSelected(selected);
        }

    }

    void refresh() {
        vm.startRefresh(true);
    }

    void asyncRefresh() {
        vm.asyncRefresh();
    }

    public void stopRefresh() {
        vm.stopRefresh();
    }

    void exitEditor() {
        list.deselect();
        scene.getMainPane().openDefaultScene();
    }

    VersionSyncInfo getSelected() {
        return selected != null && selected.size() == 1 ? selected.get(0) : null;
    }

    List<VersionSyncInfo> getSelectedList() {
        return selected;
    }

    public void block(Object reason) {
        Blocker.block(reason, list, scene.getMainPane().defaultScene);
    }

    public void unblock(Object reason) {
        Blocker.unblock(reason, list, scene.getMainPane().defaultScene);
    }

    public void onVersionRefreshing(VersionManager vm) {
        Blocker.block(instance, "refresh");

        for (VersionHandlerListener listener : listeners) {
            listener.onVersionRefreshing(vm);
        }

    }

    public void onVersionRefreshed(VersionManager vm) {
        Blocker.unblock(instance, "refresh");

        for (VersionHandlerListener listener : listeners) {
            listener.onVersionRefreshed(vm);
        }

    }

    public void onVersionSelected(List<VersionSyncInfo> version) {
        selected = version;
        if (version != null && !version.isEmpty() && version.get(0).getID() != null) {

            for (VersionHandlerListener listener : listeners) {
                listener.onVersionSelected(version);
            }
        } else {
            onVersionDeselected();
        }

    }

    public void onVersionDeselected() {
        selected = null;

        for (VersionHandlerListener listener : listeners) {
            listener.onVersionDeselected();
        }

    }

    public void onVersionDownload(List<VersionSyncInfo> list) {
        downloading = list;

        for (VersionHandlerListener listener : listeners) {
            listener.onVersionDownload(list);
        }

    }
}
