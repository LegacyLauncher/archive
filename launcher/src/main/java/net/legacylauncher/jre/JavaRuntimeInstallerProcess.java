package net.legacylauncher.jre;

import java.io.IOException;

public interface JavaRuntimeInstallerProcess {
    void install(ProgressReporter reporter) throws IOException, InterruptedException;
}
