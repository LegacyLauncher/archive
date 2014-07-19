package net.minecraft.launcher.process;

public abstract interface JavaProcessListener {
	public abstract void onJavaProcessLog(JavaProcess jp, String line);

	public abstract void onJavaProcessEnded(JavaProcess jp);

	public abstract void onJavaProcessError(JavaProcess jp, Throwable e);
}