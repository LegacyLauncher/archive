package com.turikhay.tlauncher.ui;

import java.awt.Frame;

import javax.swing.JOptionPane;

public class Alert {
	private static boolean show = false;
	
	public static void showError(String title, String message, Throwable e, boolean exit){
		if(show) return; show = true;
		Frame frame = new Frame();
		String 	t_title = "TLauncher : " + title,
				t_message = message + ((e!=null)? "\n" + stackTrace(e) : "" );
		
		frame.requestFocus();
		JOptionPane.showMessageDialog(frame, t_message, t_title, JOptionPane.ERROR_MESSAGE);
		
		show = false;
		if(exit) System.exit(1);
	}
	public static void showError(String message, Throwable e, boolean exit){ showError("An error occurred", message, e, exit); }
	public static void showError(Throwable e, boolean exit){ showError("An unexpected error occurred", e, exit); }
	
	public static void showError(String title, String message, Throwable e){ showError(title, message, e, false); }
	public static void showError(String message, Throwable e){ showError(message, e, false); }
	public static void showError(Throwable e){ showError(e, false); }
	
	public static void showError(String title, String message){ showError(title, message, null, false); }
	public static void showError(String message){ showError(message, null, false); }
	
	public static void showWarning(String title, String message){
		if(show) return; show = true;
		Frame frame = new Frame();
		String 	t_title = "TLauncher : " + title, t_message = message;
		
		frame.requestFocus();
		JOptionPane.showMessageDialog(frame, t_message, t_title, JOptionPane.WARNING_MESSAGE);
		
		show = false;
	}
	
	private static String stackTrace(Throwable e){
		String t = e.toString();
		if(t == null) t = "";
		
		StackTraceElement[] elems = e.getStackTrace();
		
		for(int x=0;x<elems.length;x++){
			t += "\nat " + elems[x].toString();
			
			if(x < 5) continue;
			t += "\n" + "... and "+ (elems.length - x - 1) + " more";
			break;
		}
		
		Throwable cause = e.getCause();
		
		if(cause != null){
			t += "\nCaused by: "+cause.toString();
			
			StackTraceElement[] causeelems = cause.getStackTrace();
			
			for(int x=0;x<causeelems.length;x++){
				t += "\nat " + causeelems[x].toString();
				
				if(x < 5) continue;
				t += "\n" + "... and "+ (causeelems.length - x - 1) + " more";
				break;
			}
		}
		
		return t;
		
	}
	
}
