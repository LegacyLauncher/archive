package ru.turikhay.tlauncher.bootstrap.task;

public class DummyTask extends Task<Void> {
    public DummyTask() {
        super("dummy");
    }

    @Override
    protected Void execute() throws Exception {
        int i = 0;

        while(i < 100) {
            i += 5;
            updateProgress(i / 100.0);
        }

        return null;
    }
}
