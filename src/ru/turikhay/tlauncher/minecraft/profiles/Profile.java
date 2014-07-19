package ru.turikhay.tlauncher.minecraft.profiles;

import java.io.File;

import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.versions.ReleaseType;

public class Profile {
	private static final ReleaseType[] DEFAULT_RELEASE_TYPES = new ReleaseType[] {
			ReleaseType.RELEASE, ReleaseType.SNAPSHOT, ReleaseType.OLD };

	private String name;
	private File gameDir;
	private String lastVersionId;
	private String javaDir;
	private String javaArgs;
	private Resolution resolution;
	private ReleaseType[] allowedReleaseTypes;
	private String playerUUID;
	private Boolean useHopperCrashService;
	private ActionOnClose launcherVisibilityOnGameClose;

	@Override
	public String toString() {
		return "Profile{name='" + name + "', gameDir='" + gameDir
				+ "', lastVersion='" + lastVersionId + "', javaDir='" + javaDir
				+ "', javaArgs='" + javaArgs + "', resolution='" + resolution
				+ "', playerUUID=" + playerUUID + ", useHopper='"
				+ useHopperCrashService + "', onClose='"
				+ launcherVisibilityOnGameClose + "'}";
	}

	public Profile(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getGameDir() {
		return this.gameDir;
	}

	public void setGameDir(File gameDir) {
		this.gameDir = gameDir;
	}

	public void setLastVersionId(String lastVersionId) {
		this.lastVersionId = lastVersionId;
	}

	public void setJavaDir(String javaDir) {
		this.javaDir = javaDir;
	}

	public void setJavaArgs(String javaArgs) {
		this.javaArgs = javaArgs;
	}

	public String getLastVersionId() {
		return this.lastVersionId;
	}

	public String getJavaArgs() {
		return this.javaArgs;
	}

	public String getJavaPath() {
		return this.javaDir;
	}

	public Resolution getResolution() {
		return this.resolution;
	}

	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}

	public String getPlayerUUID() {
		return this.playerUUID;
	}

	public void setPlayerUUID(String playerUUID) {
		this.playerUUID = playerUUID;
	}

	public ReleaseType[] getAllowedReleaseTypes() {
		return this.allowedReleaseTypes;
	}

	public void setAllowedReleaseTypes(ReleaseType[] allowedReleaseTypes) {
		this.allowedReleaseTypes = allowedReleaseTypes;
	}

	public boolean getUseHopperCrashService() {
		return this.useHopperCrashService == null;
	}

	public void setUseHopperCrashService(boolean useHopperCrashService) {
		this.useHopperCrashService = (useHopperCrashService ? null : Boolean
				.valueOf(false));
	}

	public VersionFilter getVersionFilter() {
		VersionFilter filter = new VersionFilter();
		filter.onlyForTypes(this.allowedReleaseTypes == null ? DEFAULT_RELEASE_TYPES
				: this.allowedReleaseTypes);
		return filter;
	}

	public ActionOnClose getLauncherVisibilityOnGameClose() {
		return this.launcherVisibilityOnGameClose;
	}

	public void setLauncherVisibilityOnGameClose(
			ActionOnClose launcherVisibilityOnGameClose) {
		this.launcherVisibilityOnGameClose = launcherVisibilityOnGameClose;
	}

	public static class Resolution {
		private int width;
		private int height;

		public Resolution() {
		}

		public Resolution(Resolution resolution) {
			this(resolution.getWidth(), resolution.getHeight());
		}

		public Resolution(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public int getWidth() {
			return this.width;
		}

		public int getHeight() {
			return this.height;
		}

		@Override
		public String toString() {
			return width + "x" + height;
		}
	}

	public enum ActionOnClose {
		HIDE_LAUNCHER("Hide launcher and re-open when game closes"), CLOSE_LAUNCHER(
				"Close launcher when game starts"), DO_NOTHING(
				"Keep the launcher open");

		private final String name;

		private ActionOnClose(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}
}
