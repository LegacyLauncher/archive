package ru.turikhay.tlauncher.configuration;

import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.tlauncher.managers.GPUManager;
import ru.turikhay.tlauncher.managers.JavaManagerConfig;
import ru.turikhay.tlauncher.ui.FlatLaf;
import ru.turikhay.util.Direction;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;

import java.lang.ref.WeakReference;
import java.util.*;

public final class ConfigurationDefaults {
    private static WeakReference<ConfigurationDefaults> ref;

    public static ConfigurationDefaults getInstance() {
        ConfigurationDefaults instance;

        if (ref == null || (instance = ref.get()) == null) {
            instance = new ConfigurationDefaults();
            ref = new WeakReference<>(instance);
        }

        return instance;
    }

    private static final int VERSION = 3;
    private final HashMap<String, Object> d = new HashMap<>();

    private ConfigurationDefaults() {
        d.put("settings.version", VERSION);

        d.put("minecraft.gamedir", MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath());
        d.put("minecraft.gamedir.separate", Configuration.SeparateDirs.NONE.name().toLowerCase(Locale.ROOT));

        d.put("minecraft.size", new IntegerArray(925, 530));
        d.put("minecraft.fullscreen", false);

        for (ReleaseType type : ReleaseType.getDefault()) {
            d.put("minecraft.versions." + type.name().toLowerCase(java.util.Locale.ROOT), true);
        }
        d.put("minecraft.versions.sub." + ReleaseType.SubType.REMOTE.name().toLowerCase(java.util.Locale.ROOT), true);
        d.put("minecraft.versions.sub." + ReleaseType.SubType.OLD_RELEASE.name().toLowerCase(java.util.Locale.ROOT), true);
        d.put("minecraft.versions.only-installed", false);

        d.put("minecraft.jre.type", JavaManagerConfig.Recommended.TYPE);

        d.put("minecraft.javaargs", null);
        d.put("minecraft.args", null);
        d.put("minecraft.improvedargs", true);
        d.put("minecraft.gpu", GPUManager.GPU.DISCRETE.getName());
        if (OS.LINUX.isCurrent()) {
            d.put("minecraft.gamemode", true);
        }

        d.put("minecraft.xmx", "auto");

        d.put("minecraft.servers.promoted", true);
        d.put("minecraft.servers.promoted.ingame", true);

        d.put("minecraft.onlaunch", Configuration.ActionOnLaunch.HIDE);

        d.put("minecraft.crash", true);
        d.put("minecraft.deleteTlSkinCape", true);

        d.put("gui.font", OS.CURRENT == OS.WINDOWS ? 12 : 14);
        d.put("gui.size", new IntegerArray(OS.WINDOWS.isCurrent() ? 1000 : 1200, 600));
//        d.put("gui.systemlookandfeel", false);

        d.putAll(FlatLaf.getDefaults());

        d.put("gui.background", null);

        d.put("gui.logger", Configuration.LoggerType.getDefault());
        d.put("gui.logger.width", 720);
        d.put("gui.logger.height", 500);
        d.put("gui.logger.x", 30);
        d.put("gui.logger.y", 30);

        d.put("gui.notices.enabled", true);
        d.put("notice.promoted", true);
        d.put("notice.enabled", true);

        d.put("gui.direction.loginform", Direction.CENTER);

        d.put("client", UUID.randomUUID());

        d.put("connection.ssl", true);

        d.put("ely.globally", true);

        if (OS.WINDOWS.isCurrent()) {
            d.put("windows.dxdiag", true);
            d.put("windows.gpuperf", true);
        }

        d.put("bootstrap.switchToBeta", false);
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