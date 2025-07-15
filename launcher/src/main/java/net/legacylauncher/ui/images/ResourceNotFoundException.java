package net.legacylauncher.ui.images;

public class ResourceNotFoundException extends Exception {
    public ResourceNotFoundException(String name) {
        super(name);
    }

    public String getName() {
        return getMessage();
    }
}
