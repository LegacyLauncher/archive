package ru.turikhay.tlauncher.managers;

import ru.turikhay.util.SwingUtil;

public class SwingVersionManagerListener implements VersionManagerListener {
    private final VersionManagerListener listener;

    public SwingVersionManagerListener(VersionManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onVersionsRefreshing(VersionManager var1) {
        SwingUtil.later(() -> listener.onVersionsRefreshing(var1));
    }

    @Override
    public void onVersionsRefreshingFailed(VersionManager var1) {
        SwingUtil.later(() -> listener.onVersionsRefreshingFailed(var1));
    }

    @Override
    public void onVersionsRefreshed(VersionManager var1) {
        SwingUtil.later(() -> listener.onVersionsRefreshed(var1));
    }
}
