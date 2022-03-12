package ru.turikhay.tlauncher.user.minecraft.strategy.oareq;

public class CodeRequestErrorException extends MicrosoftOAuthCodeRequestException {
    public CodeRequestErrorException(String error, String errorDescription) {
        super(format(error, errorDescription));
    }

    private static String format(String error, String errorDescription) {
        if (errorDescription != null) {
            return error + ": " + errorDescription;
        }
        return error;
    }
}
