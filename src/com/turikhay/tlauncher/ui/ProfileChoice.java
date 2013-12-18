package com.turikhay.tlauncher.ui;

import java.awt.Choice;
import java.util.Map.Entry;

import com.turikhay.tlauncher.minecraft.events.ProfileListener;
import com.turikhay.tlauncher.minecraft.profiles.Profile;
import com.turikhay.tlauncher.minecraft.profiles.ProfileLoader;
import com.turikhay.tlauncher.minecraft.profiles.ProfileManager;

public class ProfileChoice extends Choice implements ProfileListener {
	private static final long serialVersionUID = 94188912646018620L;
	
	ProfileChoice(ProfileLoader pl){
		pl.addListener(this);
		onProfilesRefreshed(pl.getSelected());
	}
	public void onProfilesRefreshed(ProfileManager pm) {
		this.removeAll();
		
		for(Entry<String, Profile> en : pm.getProfiles().entrySet())
			this.add(en.getKey());
	}
	public void onProfileManagerChanged(ProfileManager pm) {}
}
