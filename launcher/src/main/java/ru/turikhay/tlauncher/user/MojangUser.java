package ru.turikhay.tlauncher.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.UserAuthentication;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.managers.AccountManager;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

public class MojangUser extends AuthlibUser {
    public static final String TYPE = "mojang";

    MojangUser(String clientToken, String username, UserAuthentication userAuthentication) {
        super(clientToken, username, userAuthentication);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static MojangUserJsonizer getJsonizer() {
        return new MojangUserJsonizer(new MojangAuth());
    }
}
