package ru.turikhay.tlauncher.ui.alert;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;

public class Alert {
	private static final JFrame frame = new JFrame();
	private static final String PREFIX = "TLauncher : ",
			MISSING_TITLE = "MISSING TITLE",
			MISSING_MESSAGE = "MISSING MESSAGE",
			MISSING_QUESTION = "MISSING QUESTION";
	private static String DEFAULT_TITLE = "An error occurred",
			DEFAULT_MESSAGE = "An unexpected error occurred";

	/*
	 * Error monologues
	 */

	public static void showError(String title, String message, Object textarea) {

		if(textarea instanceof Throwable)
			U.log("Showing error:", textarea);

		showMonolog(JOptionPane.ERROR_MESSAGE, title, message, textarea);
	}

	public static void showError(String title, String message) {
		showError(title, message, null);
	}

	public static void showError(String message, Object textarea) {
		showError(DEFAULT_TITLE, message, textarea);
	}

	public static void showError(Object textarea, boolean exit) {
		showError(DEFAULT_TITLE, DEFAULT_MESSAGE, textarea);
		if (exit)
			System.exit(-1);
	}

	public static void showError(Object textarea) {
		showError(textarea, false);
	}

	public static void showLocError(String titlePath, String messagePath,
			Object textarea) {
		showError(getLoc(titlePath, MISSING_TITLE),
				getLoc(messagePath, MISSING_MESSAGE), textarea);
	}

	public static void showLocError(String path, Object textarea) {
		showError(getLoc(path + ".title", MISSING_TITLE),
				getLoc(path, MISSING_MESSAGE), textarea);
	}

	public static void showLocError(String path) {
		showLocError(path, null);
	}

	public static void showLocAsyncError(final String path) {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				showLocError(path);
			}
		});
	}

	/*
	 * Message monologues
	 */

	public static void showMessage(String title, String message,
			Object textarea) {
		showMonolog(JOptionPane.INFORMATION_MESSAGE, title, message, textarea);
	}

	public static void showMessage(String title, String message) {
		showMessage(title, message, null);
	}

	public static void showLocMessage(String titlePath, String messagePath,
			Object textarea) {
		showMessage(getLoc(titlePath, MISSING_TITLE),
				getLoc(messagePath, MISSING_MESSAGE), textarea);
	}

	public static void showLocAsyncMessage(final String titlePath,
			final String messagePath, final Object textarea) {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				showLocMessage(titlePath, messagePath, textarea);
			}
		});
	}

	public static void showLocMessage(String path, Object textarea) {
		showMessage(getLoc(path + ".title", MISSING_TITLE),
				getLoc(path, MISSING_MESSAGE), textarea);
	}

	public static void showLocMessage(String path) {
		showLocMessage(path, null);
	}

	public static void showLocAsyncMessage(final String path) {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				showLocMessage(path);
			}
		});
	}

	/*
	 * Warning monologues
	 */

	public static void showWarning(String title, String message, Object textarea) {
		showMonolog(JOptionPane.WARNING_MESSAGE, title, message, textarea);
	}

	public static void showWarning(String title, String message) {
		showWarning(title, message, null);
	}

	public static void showLocWarning(String titlePath, String messagePath,
			Object textarea) {
		showWarning(getLoc(titlePath, MISSING_TITLE),
				getLoc(messagePath, MISSING_MESSAGE), textarea);
	}

	private static void showLocWarning(String titlePath, String messagePath) {
		showLocWarning(titlePath, messagePath, null);
	}

	public static void showLocAsyncWarning(final String titlePath,
			final String messagePath) {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				showLocWarning(titlePath, messagePath);
			}
		});
	}

	public static void showLocWarning(String path, Object textarea) {
		showWarning(getLoc(path + ".title", MISSING_TITLE),
				getLoc(path, MISSING_MESSAGE), textarea);
	}

	public static void showLocWarning(String path) {
		showLocWarning(path, (Object) null);
	}

	public static void showLocAsyncWarning(final String path) {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				showLocWarning(path);
			}
		});
	}

	/*
	 * Question dialogues
	 */

	public static boolean showQuestion(String title, String question,
			Object textarea) {
		return showConfirmDialog(JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, title, question, textarea) == JOptionPane.YES_OPTION;
	}

	public static boolean showQuestion(String title, String question) {
		return showQuestion(title, question, null);
	}

	public static boolean showLocQuestion(String titlePath,
			String questionPath, Object textarea) {
		return showQuestion(getLoc(titlePath, MISSING_TITLE),
				getLoc(questionPath, MISSING_QUESTION), textarea);
	}

	public static boolean showLocQuestion(String titlePath, String questionPath) {
		return showQuestion(titlePath, questionPath, null);
	}

	public static boolean showLocQuestion(String path, Object textarea) {
		return showQuestion(getLoc(path + ".title", MISSING_TITLE),
				getLoc(path, MISSING_QUESTION), null);
	}

	public static boolean showLocQuestion(String path) {
		return showLocQuestion(path, (Object) null);
	}

	//

	private static void showMonolog(int messageType, String title,
			String message, Object textarea) {
		JOptionPane.showMessageDialog(frame, new AlertPanel(message, textarea),
				getTitle(title), messageType);
	}

	private static int showConfirmDialog(int optionType, int messageType,
			String title, String message, Object textarea) {
		return JOptionPane.showConfirmDialog(frame, new AlertPanel(message,
				textarea), getTitle(title), optionType, messageType);
	}

	public static void prepareLocal() {
		DEFAULT_TITLE = getLoc("alert.error.title", DEFAULT_TITLE);
		DEFAULT_MESSAGE = getLoc("alert.error.message", DEFAULT_MESSAGE);
	}

	private static String getTitle(String title) {
		return PREFIX + ((title == null) ? MISSING_TITLE : title);
	}

	private static String getLoc(String path, String fallbackMessage) {
		String result = Localizable.get(path);
		return (result == null) ? fallbackMessage : result;
	}

}
