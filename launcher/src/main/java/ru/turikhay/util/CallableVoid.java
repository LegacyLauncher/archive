package ru.turikhay.util;

import java.util.concurrent.Callable;

public interface CallableVoid extends Callable<Void> {

    @Override
    default Void call() throws Exception {
        doCall();
        return null;
    }

    void doCall() throws Exception;
}
