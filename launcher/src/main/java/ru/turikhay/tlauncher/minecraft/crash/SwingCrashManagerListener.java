package ru.turikhay.tlauncher.minecraft.crash;

import ru.turikhay.util.SwingUtil;

public class SwingCrashManagerListener implements CrashManagerListener {
    private final CrashManagerListener listener;

    public SwingCrashManagerListener(CrashManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCrashManagerProcessing(CrashManager manager) {
        SwingUtil.wait(() -> listener.onCrashManagerProcessing(manager));
    }

    @Override
    public void onCrashManagerComplete(CrashManager manager, Crash crash) {
        SwingUtil.wait(() -> listener.onCrashManagerComplete(manager, crash));
    }

    @Override
    public void onCrashManagerCancelled(CrashManager manager) {
        SwingUtil.wait(() -> listener.onCrashManagerCancelled(manager));
    }

    @Override
    public void onCrashManagerFailed(CrashManager manager, Exception e) {
        SwingUtil.wait(() -> listener.onCrashManagerFailed(manager, e));
    }
}
