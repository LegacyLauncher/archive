package com.turikhay.tlauncher.minecraft.repository;

import com.turikhay.tlauncher.TLauncher;

public class VersionRepository extends Repository {
	public static final int DEFAULT_TIMEOUT = 10000;
	
	public static final VersionRepository
		LOCAL = new VersionRepository("local"),
		OFFICIAL = new VersionRepository("official", TLauncher.getOfficialRepo()),
		EXTRA = new VersionRepository("extra", TLauncher.getExtraRepo());
	
	public VersionRepository(String name){
		super(name, DEFAULT_TIMEOUT);
	}
		
	public VersionRepository(String name, String[] urls) {
		super(name, DEFAULT_TIMEOUT, urls);
	}
}
