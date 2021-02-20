package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv;

import ru.turikhay.util.OS;

public class DefaultExternalBrowser implements ExternalBrowser {
    @Override
    public void openUrl(String url) {
        OS.openLink(url);
    }
}
