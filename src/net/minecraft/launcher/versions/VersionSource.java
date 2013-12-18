package net.minecraft.launcher.versions;

public enum VersionSource {
	LOCAL(new String[]{null}),
	REMOTE(new String[]{
		"http://s3.amazonaws.com/Minecraft.Download/"
	}),
	EXTRA(new String[]{
		"http://5.9.120.11/update/versions/",
		"http://ru-minecraft.org/update/tlauncher/extra/",
		"http://dl.dropboxusercontent.com/u/6204017/update/versions/"
	});
	
	private final String[] repos;
	private int selected;
	private boolean isSelected;
	
	private VersionSource(String[] repos){
		this.repos = repos;
	}
	
	public int getSelected(){
		return selected;
	}
	
	public void setSelected(int pos){
		if(!isSelectable())
			throw new IllegalStateException();
		
		this.isSelected = true;
		this.selected = pos;
	}
	
	public String getSelectedRepo(){
		return repos[selected];
	}
	
	public String getRepo(int pos){
		return repos[pos];
	}
	
	public String[] getRepos(){
		return repos;
	}
	
	public int getRepoCount(){
		return repos.length;
	}
	
	public boolean isSelected(){
		return isSelected;
	}
	
	public boolean isSelectable(){
		return repos.length > 1;
	}
	
}
