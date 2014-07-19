package ru.turikhay.tlauncher.handlers;

import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
	private static ExceptionHandler instance;
	private static long gcLastCall;

	public static ExceptionHandler getInstance() {
		if (instance == null)
			instance = new ExceptionHandler();
		return instance;
	}

	private ExceptionHandler() {
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		OutOfMemoryError asOOM = Reflect.cast(e, OutOfMemoryError.class);
		if (asOOM != null && reduceMemory(asOOM))
			return;

		if (scanTrace(e))
			try {
				Alert.showError("Exception in thread " + t.getName(), e);
			} catch (Exception w) {
				System.exit(2);
			}
		else
			U.log("Hidden exception in thread " + t.getName(), e);
	}

	public static boolean reduceMemory(OutOfMemoryError e) {
		if (e == null)
			return false;

		U.log("OutOfMemory error has occurred, solving...");
		long currentTime = System.currentTimeMillis(), diff = Math
				.abs(currentTime - gcLastCall);

		if (diff > 5000) {
			gcLastCall = currentTime;

			U.gc();

			return true;
		}

		U.log("GC is unable to reduce memory usage");
		return false;
	}

	private static boolean scanTrace(Throwable e) {
		StackTraceElement[] elements = e.getStackTrace();

		for (StackTraceElement element : elements)
			if (element.getClassName().startsWith(U.PROGRAM_PACKAGE))
				return true;

		return false;
	}
}
