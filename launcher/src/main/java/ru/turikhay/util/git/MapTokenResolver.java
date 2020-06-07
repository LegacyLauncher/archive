package ru.turikhay.util.git;

import java.util.Map;

public class MapTokenResolver implements ITokenResolver {
    private final Map<String, String> tokenMap;

    public MapTokenResolver(Map<String, String> tokenMap) {
        this.tokenMap = tokenMap;
    }

    public Map<String, String> getTokenMap() {
        return tokenMap;
    }

    public String resolveToken(String tokenName) {
        return tokenMap.get(tokenName);
    }
}
