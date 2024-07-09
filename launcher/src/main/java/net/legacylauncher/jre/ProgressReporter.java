package net.legacylauncher.jre;

public interface ProgressReporter {
    void reportProgress(long current, long max);
}
