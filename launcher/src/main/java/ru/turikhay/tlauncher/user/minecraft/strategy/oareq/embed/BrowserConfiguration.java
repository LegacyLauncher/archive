package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.embed;

import java.awt.*;
import java.util.List;

public class BrowserConfiguration {
    private final String title;
    private final List<Image> favicons;
    private final boolean clearCookies;

    public BrowserConfiguration(String title, List<Image> favicons, boolean clearCookies) {
        this.title = title;
        this.favicons = favicons;
        this.clearCookies = clearCookies;
    }

    public BrowserConfiguration(String title, List<Image> favicons) {
        this(title, favicons, true);
    }

    public String getTitle() {
        return title;
    }

    public List<Image> getFavicons() {
        return favicons;
    }

    public boolean isClearCookies() {
        return clearCookies;
    }
}
