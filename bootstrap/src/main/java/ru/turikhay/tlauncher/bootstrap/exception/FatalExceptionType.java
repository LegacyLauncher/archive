package ru.turikhay.tlauncher.bootstrap.exception;

import ru.turikhay.tlauncher.bootstrap.launcher.LauncherNotFoundException;
import ru.turikhay.tlauncher.bootstrap.util.OS;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum FatalExceptionType {
    INTERNET_CONNECTIVITY_BLOCKED(new TypeAssertion() {
        @Override
        public boolean ensure(Throwable t) {
            return t instanceof SocketException
                    && t.getMessage() != null
                    && t.getMessage().startsWith("Address family not supported by protocol family")
                    && OS.WINDOWS.isCurrent();
        }
    }),

    FILE_LOCKED(FileLockedException.class),

    CORRUPTED_INSTALLATION(ClassNotFoundException.class, NoClassDefFoundError.class),

    INTERNET_CONNECTIVITY(UnknownHostException.class,
            ConnectException.class,
            HttpRetryException.class,
            ProtocolException.class,
            SocketException.class,
            SocketTimeoutException.class,
            UnknownServiceException.class,
            LauncherNotFoundException.class
    );

    private final TypeAssertion assertion;
    private final List<Class> classList;

    FatalExceptionType(final Class... exceptionTypes) {
        this.assertion = null;
        this.classList = new ArrayList<Class>() {
            {
                Collections.addAll(this, exceptionTypes);
            }
        };
    }

    FatalExceptionType(TypeAssertion assertion) {
        this.assertion = assertion;
        this.classList = null;
    }

    boolean ensure(Throwable t) {
        if (t == null) {
            return false;
        }

        if (t instanceof ExceptionList) {
            List<Exception> list = ((ExceptionList) t).getList();
            if (list.isEmpty()) {
                return false;
            }

            boolean allNulls = true;
            for (Exception e : list) {
                FatalExceptionType type = getType(e);
                allNulls &= type == null;
                if (type == null || type == this) {
                    continue;
                }
                return false;
            }
            return !allNulls;
        }

        if (classList != null) {
            Class clazz = t.getClass();
            for (Class checkClazz : classList) {
                if (checkClazz.isAssignableFrom(clazz)) {
                    return true;
                }
            }
            return false;
        }

        return assertion != null && assertion.ensure(t);
    }

    public String nameLowerCase() {
        return name().toLowerCase();
    }

    public static FatalExceptionType getType(Throwable t) {
        for (FatalExceptionType type : values()) {
            if (type.ensure(t)) {
                return type;
            }
        }
        return null;
    }

    interface TypeAssertion {
        boolean ensure(Throwable t);
    }
}
