package ru.turikhay.tlauncher.configuration;

public interface Configurable {
    void load(AbstractConfiguration configuration);

    void save(AbstractConfiguration configuration);
}
