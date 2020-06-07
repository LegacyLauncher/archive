package ru.turikhay.tlauncher.ui.swing.util;

public enum Orientation {
    TOP(1),
    LEFT(2),
    BOTTOM(3),
    RIGHT(4),
    CENTER(0);

    private final int swingAlias;

    Orientation(int swingAlias) {
        this.swingAlias = swingAlias;
    }

    public int getSwingAlias() {
        return swingAlias;
    }

    public static Orientation fromSwingConstant(int orientation) {
        Orientation[] var4;
        int var3 = (var4 = values()).length;

        for (int var2 = 0; var2 < var3; ++var2) {
            Orientation current = var4[var2];
            if (orientation == current.getSwingAlias()) {
                return current;
            }
        }

        return null;
    }
}
