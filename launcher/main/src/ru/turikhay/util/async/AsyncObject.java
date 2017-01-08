package ru.turikhay.util.async;

public abstract class AsyncObject<E> extends ExtendedThread {
    private AsyncObjectContainer<E> container;

    private boolean gotValue;
    private E value;
    private AsyncObjectGotErrorException error;

    protected AsyncObject() {
        super("AsyncObject");
    }

    public final AsyncObjectContainer<E> getContainer() {
        return container;
    }

    void setContainer(AsyncObjectContainer<E> container) {
        this.container = container;
    }

    @Override
    public void run() {
        try {
            value = execute();
        } catch (Throwable e) {
            error = new AsyncObjectGotErrorException(this, e);
        }

        gotValue = true;

        if (container != null) {
            container.release();
        }
    }

    public E getValue() throws AsyncObjectNotReadyException, AsyncObjectGotErrorException {
        if (error != null)
            throw error;

        if (!gotValue)
            throw new AsyncObjectNotReadyException();

        return value;
    }

    public AsyncObjectGotErrorException getError() {
        return error;
    }

    protected abstract E execute() throws AsyncObjectGotErrorException;
}
