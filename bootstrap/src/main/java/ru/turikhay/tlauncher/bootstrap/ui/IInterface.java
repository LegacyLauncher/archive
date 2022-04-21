package ru.turikhay.tlauncher.bootstrap.ui;

import ru.turikhay.tlauncher.bootstrap.task.Task;

public interface IInterface {
    void bindToTask(Task<?> task);

    void dispose();
}
