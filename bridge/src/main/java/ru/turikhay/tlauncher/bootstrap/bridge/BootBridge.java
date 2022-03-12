package ru.turikhay.tlauncher.bootstrap.bridge;

import java.util.*;

public final class BootBridge {
    final ArrayList<BootListener> listenerList = new ArrayList<>();
    private BootEventDispatcher dispatcher;

    private final String bootstrapVersion;
    private final String[] args;
    private final Map<String, BootMessage> messageMap;
    private final Map<String, Object> capabilities;
    private String options;
    private UUID client;

    volatile boolean interrupted;

    public BootBridge(String bootstrapVersion, String[] args) {
        if(args == null) {
            args = new String[0];
        }

        this.messageMap = new HashMap<>();
        this.capabilities = new HashMap<>();
        this.bootstrapVersion = bootstrapVersion;
        this.args = args;
    }

    private BootBridge(String bootstrapVersion, String[] args, String options) {
        this(bootstrapVersion, args);
        this.options = options;
    }

    public String getBootstrapVersion() {
        return bootstrapVersion;
    }

    public String[] getArgs() {
        return args;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public UUID getClient() {
        return client;
    }

    public void setInterrupted() {
        interrupted = true;
    }

    void setClient(UUID client) {
        this.client = client;
    }

    BootMessage getMessage(String locale) {
        return messageMap.get(locale);
    }

    public void addMessage(String locale, String title, String message) {
        messageMap.put(locale, new BootMessage(title, message));
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

    public void addCapability(String key, Object value) {
        this.capabilities.put(key, value);
    }

    public void addCapability(String key) {
        addCapability(key, Boolean.TRUE);
    }

    public synchronized void addListener(BootListener listener) {
        if(listener == null) {
            throw new NullPointerException("listener");
        }
        listenerList.add(listener);
    }

    public synchronized BootEventDispatcher setupDispatcher() {
        if(this.dispatcher != null) {
            throw new IllegalStateException("dispatcher already set");
        }
        return (this.dispatcher = new BootEventDispatcher(this));
    }

    public void waitUntilClose() throws InterruptedException, BootException {
        if(this.dispatcher == null) {
            throw new IllegalStateException("no dispatcher initialized");
        }
        this.dispatcher.waitUntilClose();
    }

    public static BootBridge create(String bootstrapVersion, String[] args, String options) {
        return new BootBridge(bootstrapVersion, args, options);
    }

    public static BootBridge create(String[] args) {
        return new BootBridge(null, args, null);
    }
}
