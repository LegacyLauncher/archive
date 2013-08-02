package net.minecraft.launcher_.process;

public abstract interface JavaProcessListener
{
  public abstract void onJavaProcessEnded(JavaProcess jp);
  public abstract void onJavaProcessError(JavaProcess jp, Throwable e);
}