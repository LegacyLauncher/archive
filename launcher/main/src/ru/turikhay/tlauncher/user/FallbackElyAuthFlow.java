package ru.turikhay.tlauncher.user;

import org.apache.commons.lang3.StringUtils;

public class FallbackElyAuthFlow extends ElyAuthFlow<FallbackElyAuthFlowListener> {
    static String STATIC_PAGE_REDIRECT_NAME = "static_page_with_code";

    @Override
    protected ElyAuthCode fetchCode() throws ElyAuthStrategyException, InterruptedException {
        int state = generateState();
        openBrowser(STATIC_PAGE_REDIRECT_NAME, generateState());
        checkCancelled();
        for(FallbackElyAuthFlowListener listener : getListenerList()) {
            String result = join(listener.fallbackStrategyRequestedInput(this));
            if(StringUtils.isNotBlank(result)) {
                return new ElyAuthCode(result, STATIC_PAGE_REDIRECT_NAME, state);
            }
        }

        return null;
    }

    @Override
    protected void onCancelled() {
    }
}