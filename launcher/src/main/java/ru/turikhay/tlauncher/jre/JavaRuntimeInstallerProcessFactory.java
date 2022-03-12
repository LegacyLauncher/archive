package ru.turikhay.tlauncher.jre;

import java.io.File;

public interface JavaRuntimeInstallerProcessFactory {
    JavaRuntimeInstallerProcess createProcess(File rootDir, JavaRuntimeRemote runtimeInfo);
}
