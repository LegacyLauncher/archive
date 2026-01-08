package net.minecraft.launcher.versions;

import com.google.gson.annotations.SerializedName;

public enum ArgumentType {
    JVM,
    GAME,
    // TODO actually use these values
    @SerializedName("default-user-jvm")
    DEFAULT_USER_JVM,
}
