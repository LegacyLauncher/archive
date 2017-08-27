package ru.turikhay.tlauncher.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class McleaksStatus {
    private final List<McleaksStatusListener> listeners = Collections.synchronizedList(new ArrayList<McleaksStatusListener>());

    private final McleaksStatusThread thread;
    private String serverIp;

    McleaksStatus() {
        thread = new McleaksStatusThread(this);
        thread.start();
    }

    public void addListener(McleaksStatusListener listener) {
        listeners.add(listener);
        if(hasStatus()) {
            listener.onMcleaksUpdated(this);
        } else {
            listener.onMcleaksUpdating(this);
        }
    }

    public boolean hasStatus() {
        return !thread.isAlive();
    }

    public String getServerIp(long millis) throws TimeoutException, InterruptedException {
        waitForResponse(millis);
        return serverIp;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void waitForResponse(long millis) throws InterruptedException, TimeoutException {
        if(thread.isAlive()) {
            thread.join(millis);
            if(thread.isAlive()) {
                throw new TimeoutException();
            }
        }
    }

    void receiveStatus(McleaksStatusThread.Payload payload) {
        serverIp = payload.serverip;
        McleaksManager.getConnector().receiveServerIp(serverIp);

        for(McleaksStatusListener listener : listeners) {
            listener.onMcleaksUpdated(this);
        }
    }
}
