package net.legacylauncher.user.minecraft.strategy.oareq.lcserv;

import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import net.legacylauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface ILocalServer {
    LocalServerSelectedConfiguration start(LocalServerConfiguration configuration)
            throws LocalServerException;

    MicrosoftOAuthExchangeCode waitForCode(long time, TimeUnit timeUnit)
            throws MicrosoftOAuthCodeRequestException,
            TimeoutException, InterruptedException;

    void stop();
}
