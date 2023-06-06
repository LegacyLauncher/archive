package ru.turikhay.tlauncher.ui.theme;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

class ColorfulIcons {

    static Color getColor(String iconName) {
        return COLORS.getOrDefault(iconName, DEFAULT_COLOR);
    }

    private static final Color
            DEFAULT_COLOR = new Color(0x111111), GREEN = new Color(0x00ce00), EMERALD = new Color(0x00cd5e), DARKER_GREEN = new Color(0x009a00), RED = new Color(0xb40000), YELLOW = new Color(0xffcd00), ORANGE = new Color(0xff9a00), DARKER_ORANGE = new Color(0xff6600), BLUE = new Color(0x439aff);
    private final static Map<String, Color> COLORS = new HashMap<>();

    static {
        register("bug", EMERALD);
        register("check-square", GREEN);
        register("comments-o", BLUE);
        register("compress", BLUE);
        register("download", GREEN);
        register("envelope-open", BLUE);
        register("folder-open", ORANGE);
        register("home", DARKER_ORANGE);
        register("info-circle", BLUE);
        register("life-ring", BLUE);
        register("lightbulb-o", ORANGE);
        register("minus", RED);
        register("pencil-square", BLUE);
        register("play-circle-o-1", BLUE);
        register("plus-square", GREEN);
        register("refresh", DARKER_GREEN);
        register("remove", RED);
        register("stop-circle-o", RED);
        register("warning", ORANGE);
        register("warning-1", RED);
        register("plug-1", ORANGE);
        register("hourglass-start", ORANGE);
        register("question", BLUE);
        register("gift-1", DARKER_ORANGE);
    }

    private static void register(String iconName, Color color) {
        COLORS.put(iconName, color);
    }

}
