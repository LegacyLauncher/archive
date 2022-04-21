package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.exceptions.ParseException;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;

import java.lang.reflect.Type;
import java.util.UUID;

public class GsonParser<V extends Validatable> implements Parser<V> {
    private final Gson gson;
    private final Type type;

    public GsonParser(Gson gson, Type type) {
        this.gson = gson;
        this.type = type;
    }

    @Override
    public V parseResponse(Logger logger, String response) throws ParseException {
        logger.trace("Parsing response");
        V result;
        try {
            result = gson.fromJson(response, type);
        } catch (RuntimeException e) {
            throw new ParseException(e);
        }
        logger.trace("Validating response");
        try {
            result.validate();
        } catch (ParseException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new ParseException(e);
        }
        return result;
    }

    private static GsonBuilder gsonBuilder() {
        return new GsonBuilder();
    }

    public static <V extends Validatable> GsonParser<V> defaultParser(Type type) {
        return new GsonParser<>(
                gsonBuilder().create(),
                type
        );
    }

    public static <V extends Validatable> GsonParser<V> withDashlessUUIDAdapter(Type type) {
        return new GsonParser<>(
                gsonBuilder()
                        .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
                        .create(),
                type
        );
    }

    public static <V extends Validatable> GsonParser<V> lowerCaseWithUnderscores(Type type) {
        return new GsonParser<>(
                gsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create(),
                type
        );
    }

    public static <V extends Validatable> GsonParser<V> upperCamelCase(Type type) {
        return new GsonParser<>(
                gsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
                        .create(),
                type
        );
    }

    public static <V extends Validatable> GsonParser<V> withDeserializer(Type type, Object typeAdapter) {
        return new GsonParser<>(
                gsonBuilder()
                        .registerTypeAdapter(type, typeAdapter)
                        .create(),
                type
        );
    }
}
