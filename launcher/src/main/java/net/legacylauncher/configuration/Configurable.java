package net.legacylauncher.configuration;

public interface Configurable {
    void load(AbstractConfiguration configuration);

    void save(AbstractConfiguration configuration);
}
