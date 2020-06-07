package ru.turikhay.tlauncher.bootstrap.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExceptionList extends Exception {
    private final List<Exception> list;

    public ExceptionList(List<Exception> ioEList) {
        super(toString(ioEList));
        this.list = Collections.unmodifiableList(new ArrayList<Exception>(ioEList));
    }

    public final List<Exception> getList() {
        return list;
    }

    private static String toString(List<Exception> list) {
        if (list == null) {
            return null;
        }
        return list.toString();
    }
}
