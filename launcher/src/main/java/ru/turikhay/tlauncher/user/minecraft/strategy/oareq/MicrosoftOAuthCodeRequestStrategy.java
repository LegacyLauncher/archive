package ru.turikhay.tlauncher.user.minecraft.strategy.oareq;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface MicrosoftOAuthCodeRequestStrategy {
    MicrosoftOAuthExchangeCode requestMicrosoftOAuthCode(long time, TimeUnit timeUnit)
            throws MicrosoftOAuthCodeRequestException, InterruptedException, TimeoutException;

    default MicrosoftOAuthExchangeCode requestMicrosoftOAuthCode()
            throws MicrosoftOAuthCodeRequestException, InterruptedException, TimeoutException {
        return requestMicrosoftOAuthCode(5, TimeUnit.MINUTES);
    }
}
