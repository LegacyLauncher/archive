package com.turikhay.tlauncher.ui.converter;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;

import com.turikhay.tlauncher.component.managers.VersionManager;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class VersionConverter extends LocalizableStringConverter<VersionSyncInfo> {
	public static final VersionSyncInfo
		LOADING = VersionSyncInfo.createEmpty(),
		EMPTY = VersionSyncInfo.createEmpty();
	private final VersionManager vm;

	public VersionConverter(VersionManager vm) {
		super(null);
		
		if(vm == null)
			throw new NullPointerException();
		
		this.vm = vm;
	}
	
	public String toString(VersionSyncInfo from) {
		if(from == null) return null;
		if(from.equals(LOADING)) return Localizable.get("versions.loading");
		if(from.equals(EMPTY)) return Localizable.get("versions.notfound.tip");
		
		String id = from.getID();
		ReleaseType type = from.getLatestVersion().getReleaseType();
		
		if(type == null || type.equals(ReleaseType.UNKNOWN)) return id;
		
		String typeF = type.toString().toLowerCase();
		String formatted = Localizable.get().nget("version." + typeF, id);
		
		if(formatted == null) return id;
		return formatted;
	}

	public VersionSyncInfo fromString(String from) {
		return vm.getVersionSyncInfo(from);
	}

	@Override
	public String toValue(VersionSyncInfo from) {
		return null;
	}

	@Override
	public String toPath(VersionSyncInfo from) {
		return null;
	}

}
