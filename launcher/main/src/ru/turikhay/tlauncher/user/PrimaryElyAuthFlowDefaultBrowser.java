package ru.turikhay.tlauncher.user;

import ru.turikhay.util.OS;

import java.net.URL;

public class PrimaryElyAuthFlowDefaultBrowser implements PrimaryElyAuthFlowBrowser {
    @Override
    public boolean openLink(URL url) {
        return OS.openLink(url, false);
    }
}
