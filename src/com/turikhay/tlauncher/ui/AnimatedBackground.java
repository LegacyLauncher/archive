package com.turikhay.tlauncher.ui;

public interface AnimatedBackground {
	public void start();
	public void stop();
	public void suspend();
	public boolean isAllowed();
	public void setAllowed(boolean b);
}
