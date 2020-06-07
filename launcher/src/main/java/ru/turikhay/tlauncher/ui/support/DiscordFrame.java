package ru.turikhay.tlauncher.ui.support;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.OS;

public class DiscordFrame extends SupportFrame {
    DiscordFrame() {
        super("discord", "discord.png");
    }

    @Override
    public void openUrl() {
        OS.openLink("https://tlaun.ch/discord/support/" + TLauncher.getInstance().getSettings().getLocale().toString());
    }

    boolean isApplicable() {
        return !TLauncher.getInstance().getSettings().isUSSRLocale();
    }
}
