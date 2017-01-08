package ru.turikhay.tlauncher.minecraft.auth;

public class UserProperty {
    private String name;
    private String value;

    public UserProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getKey() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "Property{" + name + " = " + value + "}";
    }
}
