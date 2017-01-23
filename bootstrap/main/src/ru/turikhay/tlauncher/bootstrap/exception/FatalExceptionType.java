package ru.turikhay.tlauncher.bootstrap.exception;

import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum FatalExceptionType {
    CORRUPTED_INSTALLATION(ClassNotFoundException.class),
    INTERNET_CONNECTIVITY(UnknownHostException.class,
            ConnectException.class,
            HttpRetryException.class,
            ProtocolException.class,
            SocketTimeoutException.class,
            UnknownServiceException.class
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
        if(t == null) {
            return null;
        }

        Class clazz = t.getClass();

        for(FatalExceptionType type : values()) {
            if(type.classList.contains(clazz)) {
                return type;
            }
        }

        for(FatalExceptionType type : values()) {
            for(Class checkClazz : type.classList) {
                if(checkClazz.isAssignableFrom(clazz)) {
                    return type;
                }
            }
        }

        return null;
    }
}
