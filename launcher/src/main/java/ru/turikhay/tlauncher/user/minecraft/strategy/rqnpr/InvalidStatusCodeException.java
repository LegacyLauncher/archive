package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import org.apache.commons.lang3.StringUtils;

public class InvalidStatusCodeException extends InvalidResponseException {
    private final int statusCode;

    public InvalidStatusCodeException(int statusCode, String response) {
        super(response, format(statusCode, response));
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    private static String format(int statusCode, String response) {
        if (StringUtils.isEmpty(response)) {
            return String.valueOf(statusCode);
        } else {
            return statusCode + ": \"" + response + "\"";
        }
    }
}
