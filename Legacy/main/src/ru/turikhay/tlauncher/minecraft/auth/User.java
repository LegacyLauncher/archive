package ru.turikhay.tlauncher.minecraft.auth;

import java.util.List;
import java.util.Map;

public class User {
    private String id;
    private List<Map<String, String>> properties;

    public String getID() {
        return id;
    }

    public List<Map<String, String>> getProperties() {
        return properties;
    }
}
