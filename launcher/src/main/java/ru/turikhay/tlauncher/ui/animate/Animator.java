package ru.turikhay.tlauncher.ui.animate;

import java.awt.*;

public class Animator {
    private static final int DEFAULT_TICK = 20;

    public static void move(Component comp, int destX, int destY, int tick) {
        comp.setLocation(destX, destY);
    }

    public static void move(Component comp, int destX, int destY) {
        move(comp, destX, destY, 20);
    }
}
