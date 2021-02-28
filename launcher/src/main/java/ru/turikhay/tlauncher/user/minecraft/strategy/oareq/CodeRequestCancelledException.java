package ru.turikhay.tlauncher.user.minecraft.strategy.oareq;

public class CodeRequestCancelledException extends MicrosoftOAuthCodeRequestException {
    public CodeRequestCancelledException(String message) {
        super(message);
    }
}
