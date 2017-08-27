package ru.turikhay.tlauncher.user;

import com.mojang.authlib.UserAuthentication;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class McleaksUser extends AuthlibUser {
    public static final String TYPE = "mcleaks";

    private String altToken;

    McleaksUser(String altToken, String clientToken, UserAuthentication userAuthentication) {
        super(clientToken, userAuthentication.getSelectedProfile().getName(), userAuthentication);
        this.altToken = altToken;
    }

    public String getAltToken() {
        return altToken;
    }

    public void setAltToken(String altToken) {
        this.altToken = altToken;
    }

    @Override
    protected ToStringBuilder toStringBuilder() {
        return super.toStringBuilder().append("altToken", altToken);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static McleaksUserJsonizer getJsonizer() {
        return new McleaksUserJsonizer(new McleaksAuth());
    }
}
