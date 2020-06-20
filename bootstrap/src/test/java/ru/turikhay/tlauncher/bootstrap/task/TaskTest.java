package ru.turikhay.tlauncher.bootstrap.task;

import org.testng.annotations.Test;

public class TaskTest {
    @Test
    public void testUpdateProgress() throws Exception {
        DummyTask dummy = new DummyTask();
        BindTask<Void> bind = new BindTask<Void>(dummy, 0., .5);
        bind.call();
    }

}