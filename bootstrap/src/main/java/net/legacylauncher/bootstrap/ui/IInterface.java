package net.legacylauncher.bootstrap.ui;

import net.legacylauncher.bootstrap.task.Task;

public interface IInterface {
    void bindToTask(Task<?> task);

    void dispose();
}
