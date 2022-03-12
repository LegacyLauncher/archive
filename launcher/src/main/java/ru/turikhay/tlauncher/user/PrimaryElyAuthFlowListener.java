package ru.turikhay.tlauncher.user;

import java.net.URI;
import java.net.URL;

public interface PrimaryElyAuthFlowListener extends ElyAuthFlowListener {
    void primaryStrategyServerCreated(PrimaryElyAuthFlow strategy, int port);

    void primaryStrategyUrlOpened(PrimaryElyAuthFlow strategy, URL url);

    ElyFlowWaitTask<Boolean> primaryStrategyCodeParseFailed(PrimaryElyAuthFlow strategy, URI uri);
}
