package ru.turikhay.tlauncher.user;

import ru.turikhay.util.StringUtil;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class PlainAuth implements Auth<PlainUser> {

    public PlainUser authorize(String username) {
        StringUtil.requireNotBlank(username, "username");
        UUID bukkitLikeUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
        return new PlainUser(username, bukkitLikeUUID);
    }

    @Override
    public void validate(PlainUser user) {
        // always valid
    }

}
