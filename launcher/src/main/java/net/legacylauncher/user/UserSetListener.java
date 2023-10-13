package net.legacylauncher.user;

import net.legacylauncher.managed.ManagedListener;
import net.legacylauncher.managed.ManagedSet;

public interface UserSetListener extends ManagedListener<User> {
    void userSetChanged(UserSet set);

    default void changedSet(ManagedSet<User> set) {
        userSetChanged((UserSet) set);
    }
}
