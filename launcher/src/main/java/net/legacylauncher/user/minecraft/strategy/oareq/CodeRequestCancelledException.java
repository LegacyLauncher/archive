package net.legacylauncher.user.minecraft.strategy.oareq;

public class CodeRequestCancelledException extends MicrosoftOAuthCodeRequestException {
    public CodeRequestCancelledException(String message) {
        super(message);
    }
}
