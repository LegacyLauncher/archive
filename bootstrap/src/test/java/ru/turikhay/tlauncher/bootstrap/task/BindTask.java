package ru.turikhay.tlauncher.bootstrap.task;

public class BindTask<T> extends Task<T> {
    private final Task<T> task;
    private final double start, end;

    public BindTask(Task<T> task, double start, double end) {
        super("bind");
        this.task = task;
        this.start = start;
        this.end = end;
    }

    @Override
    protected T execute() throws Exception {
        return bindTo(task, start, end);
    }
}
