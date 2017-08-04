package ru.turikhay.tlauncher.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.UserAuthentication;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

public class MojangUser extends User {
    public static final String TYPE = "mojang";

    private final String clientToken, username;
    private final com.mojang.authlib.UserAuthentication userAuthentication;

    MojangUser(String clientToken, String username, com.mojang.authlib.UserAuthentication userAuthentication) {
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
    public String getType() {
        return TYPE;
    }

    @Override
    protected boolean equals(User user) {
        return user != null && getUsername().equals(user.getUsername());
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + TYPE.hashCode();
        return result;
    }

    @Override
    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("uuid", getSelectedMojangProfile().getId());
    }

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(com.mojang.authlib.properties.PropertyMap.class, new com.mojang.authlib.properties.PropertyMap.Serializer())
            .create();

    public LoginCredentials getLoginCredentials() {
        return new LoginCredentials(username,
                String.format("token:%s:%s", userAuthentication.getAuthenticatedToken(), UUIDTypeAdapter.fromUUID(getSelectedMojangProfile().getId())),
                userAuthentication.getAuthenticatedToken(),
                gson.toJson(userAuthentication.getUserProperties()),
                getDisplayName(),
                getSelectedMojangProfile().getId(),
                userAuthentication.getUserType().getName(),
                getSelectedMojangProfile().getName()
        );
    }

    public static MojangUserJsonizer getJsonizer() {
        return new MojangUserJsonizer();
    }
}
