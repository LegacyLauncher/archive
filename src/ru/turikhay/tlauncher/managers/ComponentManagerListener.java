package ru.turikhay.tlauncher.managers;

public interface ComponentManagerListener {
	public void onComponentsRefreshing(ComponentManager manager);

	public void onComponentsRefreshed(ComponentManager manager);
}
