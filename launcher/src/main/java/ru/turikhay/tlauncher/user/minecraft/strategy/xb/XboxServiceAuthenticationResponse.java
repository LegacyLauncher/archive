package ru.turikhay.tlauncher.user.minecraft.strategy.xb;

import com.google.gson.*;
import ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable;

import java.lang.reflect.Type;
import java.util.Objects;

import static ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr.Validatable.notEmpty;

public class XboxServiceAuthenticationResponse implements Validatable {
    private final String token, uhs;

    public XboxServiceAuthenticationResponse(String token, String uhs) {
        this.token = token;
        this.uhs = uhs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        XboxServiceAuthenticationResponse that = (XboxServiceAuthenticationResponse) o;

        if (!Objects.equals(token, that.token)) return false;
        return Objects.equals(uhs, that.uhs);
    }

    @Override
    public int hashCode() {
        int result = token != null ? token.hashCode() : 0;
        result = 31 * result + (uhs != null ? uhs.hashCode() : 0);
        return result;
    }

    public String getToken() {
        return token;
    }

    public String getUHS() {
        return uhs;
    }

    @Override
    public String toString() {
        return "XboxServiceAuthenticationResponse{" +
                "token='" + token + '\'' +
                ", uhs='" + uhs + '\'' +
                '}';
    }

    @Override
    public void validate() {
        notEmpty(token, "token");
        notEmpty(uhs, "uhs");
    }

    public static class Deserializer implements JsonDeserializer<XboxServiceAuthenticationResponse> {
        @Override
        public XboxServiceAuthenticationResponse deserialize(JsonElement json, Type typeOfT,
                                                             JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject o = json.getAsJsonObject();
            return new XboxServiceAuthenticationResponse(
                    // $.Token
                    o.get("Token").getAsString(),
                    // $.DisplayClaims.xui[0].uhs
                    o.get("DisplayClaims").getAsJsonObject()
                            .get("xui")/* (х)уи, хи-хи */.getAsJsonArray()
                            .get(0).getAsJsonObject()
                            .get("uhs").getAsString()
            );
        }
    }
}
