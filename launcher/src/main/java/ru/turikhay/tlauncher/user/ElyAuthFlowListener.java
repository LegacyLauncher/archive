package ru.turikhay.tlauncher.user;

import java.net.URL;

public interface ElyAuthFlowListener {
    void strategyStarted(ElyAuthFlow<?> strategy);

    void strategyErrored(ElyAuthFlow<?> strategy, Exception e);

    void strategyUrlOpened(ElyAuthFlow<?> strategy, URL url);

    void strategyUrlOpeningFailed(ElyAuthFlow<?> strategy, URL url);

    void strategyCancelled(ElyAuthFlow<?> strategy);

    void strategyComplete(ElyAuthFlow<?> strategy, ElyAuthCode code);
}
