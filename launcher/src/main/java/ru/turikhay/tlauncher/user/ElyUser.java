package ru.turikhay.tlauncher.user;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.StringUtil;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class ElyUser extends User {
    public static final String TYPE = "ely";

    private final int id;
    private final Date registeredAt;

    private String username, displayName;
    //private String preferredLanguage;
    //private URL profileLink;
    private UUID uuid;

    private String accessToken, refreshToken;
    private Long expiryTime;

    ElyUser(int id, String username, String displayName, UUID uuid, Date registeredAt, String accessToken, String refreshToken, Long expiryTime) {
        if (id < 0) {
            throw new IllegalArgumentException("id");
        }

        this.id = id;
        this.registeredAt = Objects.requireNonNull(registeredAt, "registeredAt");

        setUsername(username);
        setDisplayName(displayName);
        setUUID(uuid);
        setToken(accessToken, refreshToken, expiryTime);
    }

    int getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = StringUtil.requireNotBlank(username, "username");
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = StringUtil.requireNotBlank(displayName, "displayName");
    }

    public UUID getUUID() {
        return uuid;
    }

    void setUUID(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
    }

    Date getRegisteredAt() {
        return registeredAt;
    }

    String getAccessToken() {
        return accessToken;
    }

    String getRefreshToken() {
        return refreshToken;
    }

    Long getExpiryTime() {
        return expiryTime;
    }

    void setToken(String accessToken, String refreshToken, Long expiryTime) {
        if (expiryTime != null && expiryTime < 1) {
            throw new IllegalArgumentException("expiryTime");
        }

        this.accessToken = StringUtil.requireNotBlank(accessToken, "accessToken");
        this.refreshToken = refreshToken;
        this.expiryTime = expiryTime;
    }

    void copyFrom(ElyUser user) {
        Objects.requireNonNull(user, "user");

        if (!this.equals(user)) {
            throw new IllegalArgumentException("different users cannot be merged");
        }

        setUsername(user.getUsername());
        setDisplayName(user.getDisplayName());
        setUUID(user.getUUID());
        setToken(user.getAccessToken(), user.getRefreshToken(), user.getExpiryTime());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected boolean equals(User user) {
        return user != null && ((ElyUser) user).id == id;
    }

    @Override
    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("uuid", uuid).append("expiryTime", expiryTime);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + uuid.hashCode();
        return result;
    }

    @Override
    public LoginCredentials getLoginCredentials() {
        return new LoginCredentials(username,
                accessToken,
                null,
                displayName,
                uuid,
                "mojang", // well, not exactly
                displayName
        );
    }

    public static ElyUserJsonizer getJsonizer() {
        return new ElyUserJsonizer();
    }
}
