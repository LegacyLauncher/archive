package net.legacylauncher.user;

public interface FallbackElyAuthFlowListener extends ElyAuthFlowListener {
    ElyFlowWaitTask<String> fallbackStrategyRequestedInput(FallbackElyAuthFlow strategy);
}
