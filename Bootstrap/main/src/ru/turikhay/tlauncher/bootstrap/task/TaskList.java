package ru.turikhay.tlauncher.bootstrap.task;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

public final class TaskList extends Task<Void> {
    private final ExecutorService service;
    private final boolean shutdownAfter;

    private final ArrayList<Future> taskList = new ArrayList<Future>();
    private Future[] taskArray;

    public TaskList(String name, ExecutorService service, boolean shutdownAfter) {
        super("List:" + name);
        this.service = U.requireNotNull(service, "service");
        this.shutdownAfter = shutdownAfter;
    }

    public TaskList(String name) {
        this(name, Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setUncaughtExceptionHandler(ExceptionHandler.get());
                return t;
            }
        }), true);
    }

    public <T> Future<T> submit(Task<T> task) {
        if (this == task) {
            throw new IllegalArgumentException("cannot add task list to itself");
        }
        Future<T> future = service.submit(task);
        taskList.add(future);
        return future;
    }

    @Override
    protected Void execute() throws Exception {
        taskArray = new Future[taskList.size()];
        taskList.toArray(taskArray);

        boolean allDone;
        do {
            allDone = true;

            if (Thread.interrupted()) {
                cancellAll();
            }

            for (Future task : taskArray) {
                allDone &= task.isDone();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException interrupted) {
                cancellAll();
            }
        } while (!allDone);

        if(shutdownAfter) {
            service.shutdown();
        }
        return null;
    }

    private void cancellAll() {
        for (Future task : taskArray) {
            task.cancel(true);
        }
    }

    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder()
                .append("list", taskList);
    }
}
