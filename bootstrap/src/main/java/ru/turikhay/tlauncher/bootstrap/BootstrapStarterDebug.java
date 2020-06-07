package ru.turikhay.tlauncher.bootstrap;

import java.io.FileNotFoundException;

public final class BootstrapStarterDebug {
    public static void main(String[] args) throws Exception {
        int exit = BootstrapStarter.start(args, true);
        if(exit != 0) {
            System.exit(exit);
        }
    }
}
