package ru.turikhay.util.async;

public class RunnableThread extends ExtendedThread {
    private final Runnable r;

    public RunnableThread(String name, Runnable r) {
        super(name);

        if (r == null) {
            throw new NullPointerException();
        } else {
            this.r = r;
        }
    }

    public RunnableThread(Runnable r) {
        this("RunnableThread", r);
    }

    public void run() {
        r.run();
    }
}
