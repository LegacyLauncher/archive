package net.legacylauncher.managers;

public interface LibraryReplaceProcessorListener {
    void onLibraryReplaceRefreshing(LibraryReplaceProcessor manager);

    void onLibraryReplaceRefreshed(LibraryReplaceProcessor manager);
}
