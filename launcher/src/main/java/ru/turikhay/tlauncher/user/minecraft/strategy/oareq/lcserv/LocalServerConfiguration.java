package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv;

import java.util.List;

public class LocalServerConfiguration {
    private final String host;
    private final List<Integer> allowedPorts;
    private final String path;
    private final String redirectOnSuccess;

    private LocalServerSelectedConfiguration selectedConfiguration;

    public LocalServerConfiguration(String host, List<Integer> allowedPorts, String path, String redirectOnSuccess) {
        this.host = host;
        this.allowedPorts = allowedPorts;
        this.path = path;
        this.redirectOnSuccess = redirectOnSuccess;
    }

    public String getHost() {
        return host;
    }

    public List<Integer> getAllowedPorts() {
        return allowedPorts;
    }

    public String getPath() {
        return path;
    }

    public String getRedirectOnSuccess() {
        return redirectOnSuccess;
    }

    public LocalServerSelectedConfiguration getSelectedConfiguration() {
        return selectedConfiguration;
    }

    public void setSelectedConfiguration(LocalServerSelectedConfiguration selectedConfiguration) {
        this.selectedConfiguration = selectedConfiguration;
    }
}
