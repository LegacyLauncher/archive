package com.turikhay.tlauncher.minecraft.repository;

import com.turikhay.tlauncher.TLauncher;

public class AssetsRepository extends Repository {
	
	public static final AssetsRepository
		DEFAULT = new AssetsRepository("assets", TLauncher.getAssetsRepo());
	
	public AssetsRepository(String name, String[] urls) {
		super(name, urls);
	}

}
