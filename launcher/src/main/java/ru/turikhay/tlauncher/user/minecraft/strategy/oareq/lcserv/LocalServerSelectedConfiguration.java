package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv;

public class LocalServerSelectedConfiguration {
    private final String host;
    private final int port;
    private final String path;
    private final String state;

    public LocalServerSelectedConfiguration(String host, int port, String path, String state) {
        this.host = host;
        this.port = port;
        this.path = path;
        this.state = state;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    public String getState() {
        return state;
    }
}
