package ru.turikhay.tlauncher.ui.support;

import ru.turikhay.tlauncher.TLauncher;

public class VkSupportFrame extends SupportFrame {
    public VkSupportFrame() {
        super("vk", "vk.png", "http://tlaun.ch/vk/support?from=frame");
    }

    boolean isApplicable() {
        return TLauncher.getInstance().getSettings().isUSSRLocale();
    }
}
