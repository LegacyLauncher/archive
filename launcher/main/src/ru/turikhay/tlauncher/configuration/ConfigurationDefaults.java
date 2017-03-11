package ru.turikhay.tlauncher.configuration;

import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.util.Direction;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ConfigurationDefaults {
    private static WeakReference<ConfigurationDefaults> ref;

    public static ConfigurationDefaults getInstance() {
        ConfigurationDefaults instance;

        if (ref == null || (instance = ref.get()) == null) {
            instance = new ConfigurationDefaults();
            ref = new WeakReference<ConfigurationDefaults>(instance);
        }

        return instance;
    }

    private static final int VERSION = 3;
    private final HashMap<String, Object> d = new HashMap<String, Object>();

    private ConfigurationDefaults() {
        d.put("settings.version", VERSION);

        d.put("minecraft.gamedir", MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath());
        d.put("minecraft.gamedir.separate", false);

        d.put("minecraft.size", new IntegerArray(925, 530));
        d.put("minecraft.fullscreen", false);

        for (ReleaseType type : ReleaseType.getDefault()) {
            d.put("minecraft.versions." + type.name().toLowerCase(), true);
        }
        d.put("minecraft.versions.sub." + ReleaseType.SubType.REMOTE.name().toLowerCase(), true);

        d.put("minecraft.javaargs", null);
        d.put("minecraft.args", null);
        d.put("minecraft.improvedargs", true);
        d.put("minecraft.cmd", null);
        d.put("minecraft.memory", OS.Arch.PREFERRED_MEMORY);

        d.put("minecraft.onlaunch", Configuration.ActionOnLaunch.HIDE);

        d.put("minecraft.crash", true);

        d.put("gui.font", OS.CURRENT == OS.WINDOWS ? 12 : 14);
        d.put("gui.size", new IntegerArray(935, 570));
        d.put("gui.systemlookandfeel", true);

        d.put("gui.background", null);

        d.put("gui.logger", Configuration.LoggerType.getDefault());
        d.put("gui.logger.width", 720);
        d.put("gui.logger.height", 500);
        d.put("gui.logger.x", 30);
        d.put("gui.logger.y", 30);

        d.put("gui.direction.loginform", Direction.CENTER);

        d.put("client", UUID.randomUUID());

        d.put("ely.globally", true);

        if (OS.WINDOWS.isCurrent()) {
            d.put("windows.dxdiag", true);
        }
    }

    public static int getVersion() {
        return 3;
    }

    public Map<String, Object> getMap() {
        return Collections.unmodifiableMap(d);
    }

    public Object get(String key) {
        return d.get(key);
    }
}