package ru.turikhay.tlauncher.bootstrap.exception;

import ru.turikhay.tlauncher.bootstrap.launcher.LauncherNotFoundException;
import ru.turikhay.tlauncher.bootstrap.util.OS;

import java.net.*;
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
    ),

    UNKNOWN(new AnyOther());

    private final TypeAssertion assertion;

    FatalExceptionType(TypeAssertion assertion) {
        this.assertion = assertion;
    }

    FatalExceptionType(Class<?>... classes) {
        this(new ClassList(classes));
    }

    private boolean ensure(Throwable t) {
        if (t == null) {
            return false;
        }

        if (t instanceof ExceptionList) {
            List<Exception> list = ((ExceptionList) t).getList();
            if (list.isEmpty()) {
                return false;
            }
            boolean allUnknown = true;
            for (Exception e : list) {
                FatalExceptionType type = getType(e);
                allUnknown &= (type == UNKNOWN);
                if (type == UNKNOWN || type == this) {
                    continue;
                }
                return false;
            }
            return !allUnknown;
        }

        return assertion.ensure(t);
    }

    public String nameLowerCase() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public static FatalExceptionType getType(Throwable t) {
        for (FatalExceptionType type : values()) {
            if (type.ensure(t)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    interface TypeAssertion {
        boolean ensure(Throwable t);
    }

    static class ClassList implements TypeAssertion {
        private final Class<?>[] classList;

        ClassList(Class<?>... classList) {
            this.classList = classList;
        }

        @Override
        public boolean ensure(Throwable t) {
            Class<?> clazz = t.getClass();
            for (Class<?> checkClazz : classList) {
                if (checkClazz.isAssignableFrom(clazz)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class AnyOther implements TypeAssertion {
        @Override
        public boolean ensure(Throwable t) {
            return true;
        }
    }
}
