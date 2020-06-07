package ru.turikhay.tlauncher.user;

public interface FallbackElyAuthFlowListener extends ElyAuthFlowListener {
    ElyFlowWaitTask<String> fallbackStrategyRequestedInput(FallbackElyAuthFlow strategy);
}
