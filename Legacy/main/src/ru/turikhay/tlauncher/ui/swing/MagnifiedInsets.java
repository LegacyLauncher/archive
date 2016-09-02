package ru.turikhay.tlauncher.ui.swing;

import ru.turikhay.tlauncher.ui.TLauncherFrame;

import java.awt.*;

public class MagnifiedInsets extends Insets {
    /**
     * Creates and initializes a new <code>Insets</code> object with the
     * specified top, left, bottom, and right insets.
     *
     * @param top    the inset from the top.
     * @param left   the inset from the left.
     * @param bottom the inset from the bottom.
     * @param right  the inset from the right.
     */
    public MagnifiedInsets(int top, int left, int bottom, int right) {
        super((int) (top * TLauncherFrame.magnifyDimensions),
                (int) (left * TLauncherFrame.magnifyDimensions),
                (int) (bottom * TLauncherFrame.magnifyDimensions),
                (int) (right * TLauncherFrame.magnifyDimensions)
        );
    }

    public static MagnifiedInsets get(Insets insets) {
        if (insets == null)
            throw new NullPointerException();

        if (insets instanceof MagnifiedInsets)
            return (MagnifiedInsets) insets;

        return new MagnifiedInsets(insets.top, insets.left, insets.bottom, insets.right);
    }
}
