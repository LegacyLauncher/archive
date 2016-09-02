package ru.turikhay.util.async;

public class AsyncObjectGotErrorException extends AsyncObjectException {
    private static final long serialVersionUID = -1016561584766422788L;

    private final AsyncObject<?> object;

    public AsyncObjectGotErrorException(AsyncObject<?> object, Throwable error) {
        super(error);

        this.object = object;
    }

    public AsyncObject<?> getObject() {
        return object;
    }
}
