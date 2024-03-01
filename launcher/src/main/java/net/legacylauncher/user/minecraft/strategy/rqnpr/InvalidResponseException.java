package net.legacylauncher.user.minecraft.strategy.rqnpr;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class InvalidResponseException extends Exception {
    private final String response;

    public InvalidResponseException(String response) {
        super(response);
        this.response = response;
    }

    public InvalidResponseException(String response, String message) {
        super(message);
        this.response = response;
    }

    public InvalidResponseException(String response, Throwable cause) {
        super(response, cause);
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public JsonObject getResponseAsJson() {
        try {
            return new GsonBuilder().create().fromJson(response, JsonObject.class);
        } catch (RuntimeException rE) {
            return null;
        }
    }
}
