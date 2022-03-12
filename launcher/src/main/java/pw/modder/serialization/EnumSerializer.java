package pw.modder.serialization;

import com.google.gson.*;

import java.lang.reflect.Type;

public class EnumSerializer <T extends Enum> implements JsonSerializer<T>, JsonDeserializer<T> {
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            Object[] ordinals = Class.forName(typeOfT.getTypeName()).getEnumConstants();
            short ordinal = json.getAsShort();

            if (ordinal < 1 || ordinal > ordinals.length)
                throw new OrdinalOutOfBoundsException(ordinal);

            return (T) ordinals[ordinal-1];
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) return JsonNull.INSTANCE;
        return new JsonPrimitive(src.ordinal()+1);
    }

    static class OrdinalOutOfBoundsException extends RuntimeException {
        public OrdinalOutOfBoundsException(short ordinal) {
            super("Enum ordinal out of bounds: " + ordinal);
        }
    }
}

