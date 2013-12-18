package com.turikhay.tlauncher.ui;

import javax.swing.JComponent;

public class AnimateThread {
	public final static long DEFAULT_TIME = 1000;
	
	public static void animate(JComponent comp, int destX, int destY, long ms){
		comp.setLocation(destX, destY);
	}
	public static void animate(JComponent comp, int destX, int destY){ animate(comp, destX, destY, DEFAULT_TIME); }
}
