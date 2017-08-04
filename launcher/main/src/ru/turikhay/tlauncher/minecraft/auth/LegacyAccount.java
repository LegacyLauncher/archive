package ru.turikhay.tlauncher.minecraft.auth;

import com.mojang.authlib.properties.PropertyMap;

import java.util.UUID;

public class LegacyAccount {
    String type;
    String username;
    String userID;
    String displayName;
    String accessToken;
    String clientToken;
    UUID uuid;
    PropertyMap userProperties;
}
