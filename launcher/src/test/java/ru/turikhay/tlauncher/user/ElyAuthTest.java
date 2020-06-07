package ru.turikhay.tlauncher.user;

import org.testng.annotations.Test;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.U;

import java.net.URL;

import static org.testng.Assert.*;

/**
 * Created by turikhay on 01.02.2017.
 */
public class ElyAuthTest {
    @Test
    public void testCreateUser() throws Exception {
        ElyAuth auth = new ElyAuth();
        auth.getFallbackFlow().registerListener(new FallbackElyAuthFlowListener() {
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
                        return Alert.showInputQuestion("", "Enter code:");
                    }
                };
            }
        });
        ElyUser user = auth.createUser(null);
        assertNotNull(user, "user");
        U.log(user);
    }

    @Test
    public void testValidate() throws Exception {

    }

}