package ru.turikhay.tlauncher.configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ru.turikhay.tlauncher.configuration.Configuration.ActionOnLaunch;
import ru.turikhay.tlauncher.configuration.Configuration.ConnectionQuality;
import ru.turikhay.tlauncher.configuration.Configuration.ConsoleType;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.MinecraftUtil;
import net.minecraft.launcher.versions.ReleaseType;

class ConfigurationDefaults {
	private static final int version = 3;
	private final Map<String, Object> d; // defaults

	ConfigurationDefaults() {
		d = new HashMap<String, Object>();

		d.put("settings.version", version);

		d.put("login.auto", false);
		d.put("login.auto.timeout", 3);

		d.put("minecraft.gamedir", MinecraftUtil.getDefaultWorkingDirectory()
				.getAbsolutePath());
		d.put("minecraft.size", new IntegerArray(925, 530));

		for (ReleaseType type : ReleaseType.getDefinable())
			d.put("minecraft.versions." + type, true);

		d.put("minecraft.onlaunch", ActionOnLaunch.getDefault());

		d.put("gui.console", ConsoleType.getDefault());
		d.put("gui.console.width", 720);
		d.put("gui.console.height", 500);
		d.put("gui.console.x", 30);
		d.put("gui.console.y", 30);

		d.put("connection", ConnectionQuality.getDefault());
	}

	public static int getVersion() {
		return version;
	}

	public Map<String, Object> getMap() {
		return Collections.unmodifiableMap(d);
	}

	public Object get(String key) {
		return d.get(key);
	}
}
