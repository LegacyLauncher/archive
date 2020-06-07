package ru.turikhay.tlauncher.user;

import ru.turikhay.tlauncher.managed.ManagedListener;
import ru.turikhay.tlauncher.managed.ManagedSet;

public abstract class UserSetListener implements ManagedListener {
    public abstract void userSetChanged(UserSet set);

    public void changedSet(ManagedSet set) {
        userSetChanged((UserSet) set);
    }
}
