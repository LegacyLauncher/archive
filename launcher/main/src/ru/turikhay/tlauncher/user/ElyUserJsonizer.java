package ru.turikhay.tlauncher.user;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.U;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class ElyUserJsonizer extends UserJsonizer<ElyUser> {

    @Override
    public JsonObject serialize(ElyUser src, JsonSerializationContext context) {
        return (JsonObject) context.serialize(new ElySerialize(src));
    }

    @Override
    public ElyUser deserialize(JsonObject json, JsonDeserializationContext context) throws JsonParseException {
        return ((ElySerialize) context.deserialize(json, ElySerialize.class)).create();
    }

    static class ElySerialize {

        int id;
        String username;
        String displayName;
        UUID uuid;
        long registeredAt;
        String accessToken;
        String refreshToken;
        long expiryTime;

        ElySerialize(ElyUser user) {
            id = user.getId();
            username = user.getUsername();
            displayName = user.getDisplayName();
            uuid = user.getUUID();
            registeredAt = user.getRegisteredAt().getTime() / 1000L;
            accessToken = user.getAccessToken();
            refreshToken = user.getRefreshToken();
            expiryTime = user.getExpiryTime();
        }

        ElySerialize() {
        }

        ElyUser create() {
            return new ElyUser(
                    id,
                    username,
                    StringUtils.isBlank(displayName)? username : displayName,
                    uuid,
                    U.getUTC(registeredAt * 1000L).getTime(),
                    accessToken,
                    refreshToken,
                    expiryTime);
        }
    }
}
