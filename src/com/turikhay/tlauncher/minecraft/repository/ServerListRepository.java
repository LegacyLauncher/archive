package com.turikhay.tlauncher.minecraft.repository;

import com.turikhay.tlauncher.TLauncher;

public class ServerListRepository extends Repository {
	
	public static final ServerListRepository
		DEFAULT_REPOSITORY = new ServerListRepository("serverlist", TLauncher.getServerList());

	public ServerListRepository(String name, String[] urls) {
		super(name, urls);
	}

}
