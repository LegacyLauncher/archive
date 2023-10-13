package net.legacylauncher.managers;

public interface McleaksStatusListener {
    void onMcleaksUpdating(McleaksStatus status);

    void onMcleaksUpdated(McleaksStatus status);
}
