package ru.turikhay.tlauncher.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

import java.util.UUID;

public abstract class AuthlibUser extends User {
    private final String clientToken, username;
    private final com.mojang.authlib.UserAuthentication userAuthentication;

    AuthlibUser(String clientToken, String username, com.mojang.authlib.UserAuthentication userAuthentication) {
        this.clientToken = StringUtil.requireNotBlank(clientToken, "clientToken");
        this.username = StringUtil.requireNotBlank(username, "username");
        this.userAuthentication = U.requireNotNull(userAuthentication, "userAuthentication");

        if(!userAuthentication.isLoggedIn()) {
            throw new IllegalArgumentException("userAuthentication can't log in");
        }

        U.requireNotNull(userAuthentication.getSelectedProfile(), "selectedProfile");
        U.requireNotNull(userAuthentication.getUserProperties(), "userProperties");
        U.requireNotNull(userAuthentication.getUserType(), "userType");
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

    @Override
    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder();
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
