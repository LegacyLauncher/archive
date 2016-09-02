package ru.turikhay.tlauncher.bootstrap.exception;

import java.util.List;

public class ExceptionList extends Exception {
    public ExceptionList(List<Exception> ioEList) {
        super(toString(ioEList));
    }

    private static String toString(List<Exception> list) {
        if (list == null) {
            return null;
        }
        return list.toString();
    }
}
