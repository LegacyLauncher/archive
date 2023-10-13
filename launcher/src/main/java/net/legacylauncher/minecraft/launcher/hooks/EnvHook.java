package net.legacylauncher.minecraft.launcher.hooks;

import net.legacylauncher.minecraft.launcher.ProcessHook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class EnvHook implements ProcessHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvHook.class);
    private final Map<String, String> env;

    public EnvHook(Map<String, String> env) {
        this.env = new HashMap<>(env);
    }

    @Override
    public void enrichProcess(ProcessBuilder process) {
        env.forEach((key, value) -> {
            LOGGER.info("Applying env variable: {} = {}", key, value);
            process.environment().put(key, value);
        });
    }
}
