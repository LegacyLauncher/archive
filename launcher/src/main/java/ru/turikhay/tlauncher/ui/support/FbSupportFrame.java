package ru.turikhay.tlauncher.ui.support;

public class FbSupportFrame extends SupportFrame {
    public FbSupportFrame() {
        super("fb", "facebook-square.png", "http://tlaun.ch/fb?from=frame");
    }

    boolean isApplicable() {
        return true;
    }
}
