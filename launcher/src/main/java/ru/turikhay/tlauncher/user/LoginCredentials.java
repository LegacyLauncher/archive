package ru.turikhay.tlauncher.user;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

public class LoginCredentials {
    private final String username, session, accessToken, playerName, userType, profileName, properties;
    private final UUID uuid;

    LoginCredentials(String username, String accessToken, String properties,
                     String playerName, UUID uuid, String userType, String profileName) {
        this.username = StringUtil.requireNotBlank(username, "username");
        this.accessToken = StringUtil.requireNotBlank(accessToken, "accessToken");
        this.properties = StringUtils.isBlank(properties) ? "{}" : properties;
        this.playerName = StringUtil.requireNotBlank(playerName, "playerName");
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.userType = StringUtil.requireNotBlank(userType, "userType");
        this.profileName = StringUtil.requireNotBlank(profileName, "profileName");

        this.session = String.format(java.util.Locale.ROOT, "token:%s:%s", accessToken, UUIDTypeAdapter.fromUUID(uuid));
    }

    public String getUsername() {
        return username;
    }

    public String getSession() {
        return session;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getUserType() {
        return userType;
    }

    public String getProfileName() {
        return profileName;
    }

    public String getProperties() {
        return properties;
    }

    public UUID getUuid() {
        return uuid;
    }

    public LinkedHashMap<String, String> map() {
        return new LinkedHashMap<String, String>() {
            {
                put("auth_username", username);
                put("auth_session", session);
                put("auth_access_token", accessToken);
                put("user_properties", properties);
                put("auth_player_name", playerName);
                put("auth_uuid", UUIDTypeAdapter.fromUUID(uuid));
                put("user_type", userType);
                put("profile_name", profileName);
            }
        };
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("session", session)
                .append("accessToken", accessToken)
                .append("playerName", playerName)
                .append("userType", userType)
                .append("profileName", profileName)
                .append("properties", properties)
                .append("uuid", uuid)
                .build();
    }
}
