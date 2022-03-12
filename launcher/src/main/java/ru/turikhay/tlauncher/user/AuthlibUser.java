package ru.turikhay.tlauncher.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.turikhay.util.StringUtil;

import java.util.Objects;
import java.util.UUID;

public abstract class AuthlibUser extends User {
    private final String clientToken, username;
    private final com.mojang.authlib.UserAuthentication userAuthentication;

    AuthlibUser(String clientToken, String username, com.mojang.authlib.UserAuthentication userAuthentication) {
        this.clientToken = StringUtil.requireNotBlank(clientToken, "clientToken");
        this.username = StringUtil.requireNotBlank(username, "username");
        this.userAuthentication = Objects.requireNonNull(userAuthentication, "userAuthentication");

        if (!userAuthentication.isLoggedIn()) {
            throw new IllegalArgumentException("userAuthentication can't log in");
        }

        Objects.requireNonNull(userAuthentication.getSelectedProfile(), "selectedProfile");
        Objects.requireNonNull(userAuthentication.getUserProperties(), "userProperties");
        Objects.requireNonNull(userAuthentication.getUserType(), "userType");
    }

    AuthlibUser(AuthlibUserPayload payload) {
        this(payload.getClientToken(), payload.getUsername(), payload.getAuthentication());
    }

    String getClientToken() {
        return clientToken;
    }

    @Override
    public UUID getUUID() {
        return getSelectedMojangProfile().getId();
    }

    com.mojang.authlib.UserAuthentication getMojangUserAuthentication() {
        return userAuthentication;
    }

    private com.mojang.authlib.GameProfile getSelectedMojangProfile() {
        return userAuthentication.getSelectedProfile();
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getDisplayName() {
        return getSelectedMojangProfile().getName();
    }

    @Override
    protected boolean equals(User user) {
        return user != null && getUsername().equals(user.getUsername());
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }

    private static final Gson gson = new GsonBuilder()
            //.registerTypeAdapter(com.mojang.authlib.properties.PropertyMap.class, new com.mojang.authlib.properties.PropertyMap.Serializer())
            .create();

    // TODO fix properties

    public LoginCredentials getLoginCredentials() {
        return new LoginCredentials(username,
                userAuthentication.getAuthenticatedToken(),
                "{}",
                getDisplayName(),
                getSelectedMojangProfile().getId(),
                "mojang",
                getSelectedMojangProfile().getName()
        );
    }
}
