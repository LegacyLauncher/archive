package ru.turikhay.tlauncher.bootstrap.exception;

import ru.turikhay.tlauncher.bootstrap.launcher.LauncherNotFoundException;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum FatalExceptionType {
    CORRUPTED_INSTALLATION(ClassNotFoundException.class, NoClassDefFoundError.class),
    INTERNET_CONNECTIVITY(UnknownHostException.class,
            ConnectException.class,
            HttpRetryException.class,
            ProtocolException.class,
            SocketTimeoutException.class,
            UnknownServiceException.class,
            LauncherNotFoundException.class
    );

    private final List<Class> classList;

    FatalExceptionType(final Class... exceptionTypes) {
        this.classList = new ArrayList<Class>() {
            {
                Collections.addAll(this, exceptionTypes);
            }
        };
    }

    public String nameLowerCase() {
        return name().toLowerCase();
    }

    public static FatalExceptionType getType(Throwable t) {
        if (t == null) {
            return null;
        }

        if (t instanceof ExceptionList) {
            List<Exception> exceptionList = ((ExceptionList) t).getList();
            switch (exceptionList.size()) {
                case 0:
                    return null;
                case 1:
                    return exceptionList.get(0) == null ? null : getType(exceptionList.get(0));
                default:
                    FatalExceptionType selectedType = null, currentType;
                    for (int i = 0; i < exceptionList.size(); i++) {
                        if (selectedType == null) {
                            selectedType = getType(exceptionList.get(i));
                            continue;
                        }
                        currentType = getType(exceptionList.get(i));
                        if(currentType == null || selectedType == currentType) {
                            continue;
                        }
                        return null;
                    }
                    return selectedType;
            }
        }


        FatalExceptionType type = getTypeExplicitly(t.getClass());

        if (type == null && t.getCause() != null) {
            return getType(t.getCause());
        }

        return type;
    }

    private static FatalExceptionType getTypeExplicitly(Class clazz) {
        for (FatalExceptionType type : values()) {
            if (type.classList.contains(clazz)) {
                return type;
            }
        }
        for (FatalExceptionType type : values()) {
            for (Class checkClazz : type.classList) {
                if (checkClazz.isAssignableFrom(clazz)) {
                    return type;
                }
            }
        }
        return null;
    }
}
