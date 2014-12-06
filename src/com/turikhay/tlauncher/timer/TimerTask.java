package com.turikhay.tlauncher.timer;

public interface TimerTask extends Runnable {
	public boolean isRepeating();
	public int getTicks();
}
