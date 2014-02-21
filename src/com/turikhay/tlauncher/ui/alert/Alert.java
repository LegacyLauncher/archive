package com.turikhay.tlauncher.ui.alert;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;

public class Alert {
	private static final int wrap = 100;
	
	private static LangConfiguration lang;
	private static String
		PREFIX = "TLauncher : ",
		DEFAULT_TITLE = "An error occurred",
		DEFAULT_MESSAGE = "An unexpected error occurred",
		MISSING_TITLE = "MISSING TITLE",
		MISSING_MESSAGE = "MISSING MESSAGE";
	private static final boolean DEFAULT_EXIT = false;
	
	public static void showError(String title, String message, Object textarea, Throwable e, boolean exit){
		JFrame frame = new JFrame();
		String 	t_title = PREFIX + title,
				t_message = (message != null)? "<html><div align=\"justify\">" + w(message).replace("\n", "<br/>") + "</div></html>" : null,
				t_throwable =  ((e!=null)? w(U.stackTrace(e)) : null),
				t_textarea = (textarea != null)? w(textarea.toString()) : null;
		
		AlertPanel panel = new AlertPanel(t_message);
		if(t_textarea != null) panel.addTextArea(t_textarea);
		if(t_throwable != null) panel.addTextArea(t_throwable);
		
		frame.requestFocus();
		JOptionPane.showMessageDialog(frame, panel, t_title, JOptionPane.ERROR_MESSAGE);
		
		if(exit) System.exit(1);
	}
	public static void showError(String title, String message, Throwable e){ showError(title, message, null, e, DEFAULT_EXIT); }
	public static void showError(String message, Throwable e){ showError(DEFAULT_TITLE, message, null, e, DEFAULT_EXIT); }
	public static void showError(Throwable e, boolean exit){ showError(DEFAULT_TITLE, DEFAULT_MESSAGE, null, e, exit); }
	
	public static void showError(String title, String message, Object textarea){ showError(title, message, textarea, null, DEFAULT_EXIT); }
	public static void showError(String title, String message){ showError(title, message, null, null, DEFAULT_EXIT); }
	
	public static void showError(String path){ showError(getLocal(path + ".title", MISSING_TITLE), getLocal(path, MISSING_MESSAGE)); }
	
	public static void showWarning(String title, String message, Object textarea){
		JFrame frame = new JFrame();
		String 	t_title = PREFIX + title,
				t_message = (message != null)? "<html><div align=\"justify\">" + w(message).replace("\n", "<br/>") + "</div></html>" : null,
				t_textarea = (textarea != null)? w(textarea.toString()) : null;
		
		AlertPanel panel = new AlertPanel(t_message);
		if(t_textarea != null) panel.addTextArea(t_textarea);
		
		frame.requestFocus();
		JOptionPane.showMessageDialog(frame, panel, t_title, JOptionPane.WARNING_MESSAGE);
	}
	public static void showAsyncWarning(final String title, final String message, final Object textarea){ AsyncThread.execute(new Runnable(){public void run(){ showWarning(title, message, textarea); }}); }
	public static void showAsyncWarning(final String title, final String message){ showAsyncWarning(title, message, null); }
	public static void showAsyncWarning(final String path){ AsyncThread.execute(new Runnable(){public void run(){ showWarning(path); }}); }
	public static void showWarning(String title, String message){ showWarning(title, message, null); }
	public static void showWarning(String path){ showWarning(getLocal(path + ".title", MISSING_TITLE), getLocal(path, MISSING_MESSAGE), null); }
	
	public static boolean showQuestion(String title, String message, Object textarea, boolean force){
		JFrame frame = new JFrame();
		String 	t_title = PREFIX + title,
				t_message = (message != null)? "<html><div align=\"justify\">" + w(message).replace("\n", "<br/>") + "</div></html>" : null,
				t_textarea = (textarea != null)? w(textarea.toString()) : null;
				
		AlertPanel panel = new AlertPanel(t_message);
		if(t_textarea != null) panel.addTextArea(t_textarea);
		
		frame.requestFocus();
		boolean result = (JOptionPane.showConfirmDialog(frame, panel, t_title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
		
		return result;
	}
	public static boolean showQuestion(String path, Object textarea, boolean force){ return showQuestion(getLocal(path + ".title", MISSING_TITLE), getLocal(path, MISSING_MESSAGE), textarea, force); }
	public static boolean showQuestion(String path, boolean force){ return showQuestion(path, null, force); }
	
	public static void showMessage(String title, String message, Object textarea){
		JFrame frame = new JFrame();
		String 	t_title = PREFIX + title,
				t_message = (message != null)? "<html><div align=\"justify\">" + w(message).replace("\n", "<br/>") + "</div></html>" : null,
				t_textarea = (textarea != null)? w(textarea.toString()) : null;
		
		AlertPanel panel = new AlertPanel(t_message);
		if(t_textarea != null) panel.addTextArea(t_textarea);
		
		frame.requestFocus();
		JOptionPane.showMessageDialog(frame, panel, t_title, JOptionPane.INFORMATION_MESSAGE);
	}
	public static void showMessage(String path, Object textarea){ showMessage(getLocal(path + ".title", MISSING_TITLE), getLocal(path, MISSING_MESSAGE), textarea); }
	public static void showAsyncMessage(final String path, final Object textarea){ AsyncThread.execute(new Runnable(){ public void run(){ showMessage(path, textarea); } }); }
	public static void showAsyncMessage(String path){ showAsyncMessage(path, null); }
	
	private static String getLocal(String path, String message){
		try{
			if(lang == null)
				lang = TLauncher.getInstance().getLang();
			return lang.get(path);
		}catch(Throwable e){}
		
		return message;
	}
	
	public static void prepareLocal(){
		DEFAULT_TITLE = getLocal("alert.error.title", DEFAULT_TITLE);
		DEFAULT_MESSAGE = getLocal("alert.error.message", DEFAULT_MESSAGE);
	}
	
	private static String w(String s){ return U.w(s, wrap); }
	
}
