package net.legacylauncher.user.minecraft.strategy.oareq.lcserv;

import net.legacylauncher.util.OS;

public class DefaultExternalBrowser implements ExternalBrowser {
    @Override
    public void openUrl(String url) {
        OS.openLink(url);
    }
}
