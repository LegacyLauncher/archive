package ru.turikhay.tlauncher.bootstrap.exception;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

public class InternetConnectionException extends Exception {
    private InternetConnectionException(Throwable cause) {
        super(cause);
    }

    public static boolean checkIf(Exception e) {
        if(e == null) {
            return false;
        }

        if(e instanceof ExceptionList) {
            List<Exception> eList = ((ExceptionList) e).getList();
            for(Exception _e : eList) {
                if(!checkIf(_e)) {
                    return false;
                }
            }
            return true;
        }

        return e instanceof UnknownHostException ||
                e instanceof java.net.ConnectException ||
                e instanceof java.net.HttpRetryException ||
                e instanceof java.net.NoRouteToHostException ||
                e instanceof java.net.PortUnreachableException ||
                e instanceof java.net.ProtocolException ||
                e instanceof java.net.SocketTimeoutException ||
                e instanceof java.net.UnknownServiceException;
    }

    public static Exception returnIf(Exception e) {
        return checkIf(e)? new InternetConnectionException(e) : e;
    }
}
