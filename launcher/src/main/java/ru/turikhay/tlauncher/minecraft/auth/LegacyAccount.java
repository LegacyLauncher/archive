package ru.turikhay.tlauncher.minecraft.auth;

import com.mojang.authlib.properties.PropertyMap;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class LegacyAccount {
    String type;
    String username;
    String userID;
    String displayName;
    String accessToken;
    String clientToken;
    String uuid;
    PropertyMap userProperties;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("type", type)
                .append("username", username)
                .append("userID", userID)
                .append("displayName", displayName)
                .append("accessToken", accessToken != null ? accessToken.length() : 0)
                .append("clientToken", clientToken)
                .append("uuid", uuid)
                .append("userProperties", userProperties)
                .toString();
    }
}
