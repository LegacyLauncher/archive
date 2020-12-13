package ru.turikhay.tlauncher.user;

import org.testng.annotations.Test;
import ru.turikhay.util.U;

import java.net.URL;

public class FallbackElyAuthFlowTest {
    @Test
    public void testFetchCode() throws Exception {
        FallbackElyAuthFlow fallbackElyAuthStrategy = new FallbackElyAuthFlow();
        fallbackElyAuthStrategy.registerListener(new FallbackLisenerTest());
        System.out.println(fallbackElyAuthStrategy.call());
    }

    private static class FallbackLisenerTest implements FallbackElyAuthFlowListener {
        @Override
        public void strategyStarted(ElyAuthFlow strategy) {
        }

        @Override
        public void strategyErrored(ElyAuthFlow strategy, Exception e) {
        }

        @Override
        public void strategyUrlOpened(ElyAuthFlow strategy, URL url) {
        }

        @Override
        public void strategyUrlOpeningFailed(ElyAuthFlow strategy, URL url) {
        }

        @Override
        public void strategyCancelled(ElyAuthFlow strategy) {
        }

        @Override
        public void strategyComplete(ElyAuthFlow strategy, ElyAuthCode code) {
        }

        @Override
        public ElyFlowWaitTask<String> fallbackStrategyRequestedInput(FallbackElyAuthFlow strategy) {
            return new ElyFlowWaitTask<String>() {
                @Override
                public String call() throws Exception {
                    return "cae72b2d-9a68-43f8-bfe9-b663bf34cfef";
                }
            };
        }
    }

}