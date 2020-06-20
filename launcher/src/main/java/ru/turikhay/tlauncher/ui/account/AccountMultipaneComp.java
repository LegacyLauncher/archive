package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.minecraft.auth.Account;

import javax.swing.*;
import java.awt.*;

public interface AccountMultipaneComp {
    String LOC_PREFIX_PATH = "account.manager.multipane.";

    Component multipaneComp();
    String multipaneName();
    boolean multipaneLocksView();

    void multipaneShown(boolean gotBack);
}