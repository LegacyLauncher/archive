package ru.turikhay.tlauncher.user.minecraft.oauth;

public class OAuthApplication {
    public static final OAuthApplication
            OFFICIAL_LAUNCHER = new OAuthApplication("00000000402b5328", "service::user.auth.xboxlive.com::MBI_SSL", false),
            TL = new OAuthApplication("a332a5f6-c4dc-45b6-9fe3-d881490252b2", "Xboxlive.signin offline_access", true);

    private final String clientId;
    private final String scope;
    private final boolean useWeirdXboxTokenPrefix;

    public OAuthApplication(String clientId, String scope, boolean useWeirdXboxTokenPrefix) {
        this.clientId = clientId;
        this.scope = scope;
        this.useWeirdXboxTokenPrefix = useWeirdXboxTokenPrefix;
    }

    public String getClientId() {
        return clientId;
    }

    public String getScope() {
        return scope;
    }

    public boolean usesWeirdXboxTokenPrefix() {
        return useWeirdXboxTokenPrefix;
    }
}
