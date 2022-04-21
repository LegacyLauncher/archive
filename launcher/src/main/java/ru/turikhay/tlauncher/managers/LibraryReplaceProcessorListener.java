package ru.turikhay.tlauncher.managers;

public interface LibraryReplaceProcessorListener {
    void onLibraryReplaceRefreshing(LibraryReplaceProcessor manager);

    void onLibraryReplaceRefreshed(LibraryReplaceProcessor manager);
}
