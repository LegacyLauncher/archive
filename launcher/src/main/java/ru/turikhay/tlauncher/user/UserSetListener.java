package ru.turikhay.tlauncher.user;

import ru.turikhay.tlauncher.managed.ManagedListener;
import ru.turikhay.tlauncher.managed.ManagedSet;

public interface UserSetListener extends ManagedListener<User> {
    void userSetChanged(UserSet set);

    default void changedSet(ManagedSet<User> set) {
        userSetChanged((UserSet) set);
    }
}
