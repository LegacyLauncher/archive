package ru.turikhay.tlauncher.ui.theme;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class SystemTheme extends Theme {
    static {
        SwingUtil.initLookAndFeel();
    }

    static final int MAX_ARC = 64, MAX_BORDER = 24, BLACK_MIN = 64, WHITE_MAX = 192;

    private static final SystemTheme instance = new SystemTheme();

    public static SystemTheme getSystemTheme() {
        return instance;
    }

    private final JLabel component;
    private final Map<Border, Color> borderColorMap;

    private final Color
            success = new Color(78, 196, 78),
            failure = new Color(179, 0, 0),
            shadow = U.shiftAlpha(Color.gray, -155),
            semiForeground, panelBackground;

    private SystemTheme() {
        super("system");
        this.component = new JLabel();

        this.borderColorMap = new HashMap<Border, Color>();
        borderColorMap.put(Border.MAIN_PANEL, new Color(28, 128, 28, 255));
        borderColorMap.put(Border.ADDITIONAL_PANEL, new Color(255, 177, 177));
        borderColorMap.put(Border.SETTINGS_PANEL, OS.VERSION.startsWith("10.") ? new Color(217, 217, 217, 255) : new Color(172, 172, 172, 255));

        assert borderColorMap.size() == Border.values().length;

        this.semiForeground = U.shiftColor(getForeground(), 96, 64, 192);
        this.panelBackground = U.shiftAlpha(getBackground(), -176, 64, 192);
    }

    @Override
    public Color getForeground() {
        return component.getForeground();
    }

    @Override
    public Color getSemiForeground() {
        return semiForeground;
    }

    @Override
    public Color getBackground() {
        return component.getBackground();
    }

    @Override
    public Color getPanelBackground() {
        return panelBackground;
    }

    @Override
    public Color getSuccess() {
        return success;
    }

    @Override
    public Color getFailure() {
        return failure;
    }

    @Override
    public int getBorderSize() {
        return 2;
    }

    @Override
    public Color getBorder(Border border) {
        return borderColorMap.get(U.requireNotNull(border, "border"));
    }

    @Override
    public Color getShadow(Border border) {
        return shadow;
    }

    @Override
    public int getArc(Border border) {
        return border == Border.SETTINGS_PANEL? 16 : 24;
    }

    @Override
    public URL loadAsset(String name) throws IOException {
        URL resource;
        selectResouce:
        {
            Color background = getBackground();
            String subFolder = null;

            selectSubFolder:
            {
                if (background.getRed() < BLACK_MIN && background.getGreen() < BLACK_MIN && background.getBlue() < BLACK_MIN) {
                    Color foreground = getForeground();
                    if(foreground.equals(Color.WHITE)) {
                        subFolder = "white";
                    } else {
                        subFolder = "gray";
                    }
                    break selectSubFolder;
                }
                subFolder = "colorized";
            }

            resource = Images.class.getResource(subFolder + "/" + name);
            if(resource != null) {
                break selectResouce;
            }

            resource = Images.class.getResource(name);
        }
        if(resource == null) {
            throw new IOException("can't find resource: " + name);
        }
        return resource;
    }
}
