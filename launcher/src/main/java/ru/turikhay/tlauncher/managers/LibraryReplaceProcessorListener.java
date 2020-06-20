package ru.turikhay.tlauncher.managers;

import net.minecraft.launcher.versions.LibraryReplace;

public interface LibraryReplaceProcessorListener {
    void onLibraryReplaceRefreshing(LibraryReplaceProcessor manager);
    void onLibraryReplaceRefreshed(LibraryReplaceProcessor manager);
}
