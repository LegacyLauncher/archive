package ru.turikhay.tlauncher.exceptions;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IOExceptionList extends IOException {
    private final List<IOException> list, _list;

    public IOExceptionList(List<IOException> l) {
        super(describe(l));

        list = l;
        _list = l == null ? Collections.unmodifiableList(Collections.EMPTY_LIST) : Collections.unmodifiableList(new ArrayList<IOException>(l));
    }

    public final List<IOException> getList() {
        return _list;
    }

    private static String describe(List<IOException> list) {
        if (list == null || list.isEmpty()) {
            return "unknown";
        }

        StringBuilder b = new StringBuilder("(").append(list.size()).append("): [");
        for (IOException ioE : list) {
            if (b.length() > 200) {
                b.append(".....");
                break;
            }

            b.append(ioE.toString());

            if (ioE.getCause() != null) {
                b.append(" (cause: ").append(ioE.getCause()).append(")");
            }

            b.append("; ");
        }
        return b.append("]").toString();
    }
}
