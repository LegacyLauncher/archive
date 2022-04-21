package ru.turikhay.tlauncher.user;

import ru.turikhay.util.StringUtil;

import java.util.Objects;
import java.util.UUID;

public class ElyLegacyUser extends User {
    public static final String TYPE = "ely_legacy";

    private final String username;
    private final UUID uuid;
    private String displayName, clientToken, accessToken;

    public ElyLegacyUser(String username, UUID uuid, String displayName, String clientToken, String accessToken) {
        this.username = StringUtil.requireNotBlank(username, "username");
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        setDisplayName(displayName);
        setToken(clientToken, accessToken);
    }

    @Override
    public String getUsername() {
        return username;
    }

    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    void setDisplayName(String displayName) {
        this.displayName = StringUtil.requireNotBlank(displayName, "displayName");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected boolean equals(User user) {
        return username.equals(((ElyLegacyUser) user).username);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + uuid.hashCode();
        return result;
    }

    String getClientToken() {
        return clientToken;
    }

    String getAccessToken() {
        return accessToken;
    }

    void setToken(String clientToken, String accessToken) {
        this.clientToken = StringUtil.requireNotBlank(clientToken, "clientToken");
        this.accessToken = StringUtil.requireNotBlank(accessToken, "accessToken");
    }

    @Override
    public LoginCredentials getLoginCredentials() {
        return new LoginCredentials(
                username,
                accessToken,
                null,
                displayName,
                uuid,
                "mojang",
                displayName
        );
    }

    public static ElyLegacyUserJsonizer getJsonizer() {
        return new ElyLegacyUserJsonizer();
    }
}
