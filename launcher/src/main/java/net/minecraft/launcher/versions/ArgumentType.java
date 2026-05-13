package net.minecraft.launcher.versions;

import com.google.gson.annotations.SerializedName;

public enum ArgumentType {
    JVM,
    GAME,
    // TODO actually use these values
    @SerializedName(value = "default-user-jvm", alternate = {"default_user_jvm"})
    DEFAULT_USER_JVM,
}
