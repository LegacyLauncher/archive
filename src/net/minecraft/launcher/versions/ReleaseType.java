package net.minecraft.launcher.versions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ReleaseType {
	RELEASE("release", false, true), SNAPSHOT("snapshot"), OLD_BETA("old-beta"), OLD_ALPHA(
			"old-alpha"), OLD("old"), MODIFIED("modified"), UNKNOWN("unknown",
			false, false);

	private static final Map<String, ReleaseType> lookup;
	private static ReleaseType[] defineableTypes, updateableTypes;

	private final String name;
	private final boolean isDefault, isDesired;

	private ReleaseType(String name, boolean isDefault, boolean isDesired) {
		this.name = name;
		this.isDefault = isDefault;
		this.isDesired = isDesired;
	}

	private ReleaseType(String name, boolean isDesired) {
		this(name, true, isDesired);
	}

	private ReleaseType(String name) {
		this(name, true, false);
	}

	String getName() {
		return name;
	}

	public boolean isDesired() {
		return isDesired;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public boolean isOld() {
		return name.startsWith("old");
	}

	@Override
	public String toString() {
		return super.toString().toLowerCase();
	}

	public static ReleaseType getByName(String name) {
		return lookup.get(name);
	}

	public static ReleaseType[] getDefinable() {
		return defineableTypes;
	}

	public static ReleaseType[] getUpdateable() {
		return updateableTypes;
	}

	static {
		// Creating lookup
		lookup = new HashMap<String, ReleaseType>();

		for (ReleaseType type : values())
			lookup.put(type.getName(), type);

		// Creating defaultTypes
		List<ReleaseType> defTypes = new ArrayList<ReleaseType>();

		for (ReleaseType type : values())
			if (type.isDefault)
				defTypes.add(type);

		defineableTypes = new ReleaseType[defTypes.size()];
		defTypes.toArray(defineableTypes);

		// Creating updateableTypes
		updateableTypes = new ReleaseType[] { ReleaseType.RELEASE,
				ReleaseType.SNAPSHOT, ReleaseType.MODIFIED };
	}
}
