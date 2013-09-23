package net.minecraft.launcher_.versions;

public enum VersionSource {
	LOCAL, REMOTE, EXTRA;
	
	public String getDownloadPath(){
		switch(this){
		case LOCAL: return null;
		case REMOTE: return "https://s3.amazonaws.com/Minecraft.Download/";
		case EXTRA: return "https://dl.dropboxusercontent.com/u/6204017/minecraft/tlauncher/extra/";
		}
		
		throw new IllegalStateException("Unknown repo type!");
	}
}
