package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
            return (JsonObject) new JsonParser().parse(response);
        } catch (RuntimeException rE) {
            return null;
        }
    }
}
