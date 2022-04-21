package ru.turikhay.tlauncher.user.minecraft.strategy.oareq.lcserv.nanohttpd;

import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthCodeRequestException;
import ru.turikhay.tlauncher.user.minecraft.strategy.oareq.MicrosoftOAuthExchangeCode;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class LockExchange {
    private final CountDownLatch lock = new CountDownLatch(1);

    private volatile MicrosoftOAuthExchangeCode code;
    private volatile MicrosoftOAuthCodeRequestException error;

    MicrosoftOAuthExchangeCode waitForCode(long time, TimeUnit timeUnit)
            throws MicrosoftOAuthCodeRequestException, InterruptedException, TimeoutException {
        if (!lock.await(time, timeUnit)) {
            throw new TimeoutException();
        }
        if (error == null) {
            return Objects.requireNonNull(code, "code");
        }
        throw error;
    }

    void unlock(MicrosoftOAuthExchangeCode code) {
        this.code = Objects.requireNonNull(code, "code");
        done();
    }

    void unlockWithError(MicrosoftOAuthCodeRequestException error) {
        this.error = Objects.requireNonNull(error, "error");
        done();
    }

    private void done() {
        lock.countDown();
    }
}
