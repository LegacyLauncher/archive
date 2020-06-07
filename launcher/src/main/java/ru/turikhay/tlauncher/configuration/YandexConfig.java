package ru.turikhay.tlauncher.configuration;

public class YandexConfig {
    private boolean enabled;
    private String url;
    private String checksum;

    public boolean isEnabled() {
        return enabled;
    }

    public String getUrl() {
        return url;
    }

    public String getChecksum() {
        return checksum;
    }
}
