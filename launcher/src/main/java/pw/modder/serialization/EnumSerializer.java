package pw.modder.serialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class EnumSerializer<T extends Enum<T>> extends TypeAdapter<T> {
    private final T[] enums;

    public EnumSerializer(Class<T> enumClass) {
        this.enums = enumClass.getEnumConstants();
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        out.value(value.ordinal() + 1);
    }

    @Override
    public T read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        int ordinal = in.nextInt() - 1;

        if (ordinal < 0 || ordinal >= enums.length) {
            throw new OrdinalOutOfBoundsException(ordinal);
        }

        return enums[ordinal];
    }

    static class OrdinalOutOfBoundsException extends RuntimeException {
        public OrdinalOutOfBoundsException(int ordinal) {
            super("Enum ordinal out of bounds: " + ordinal);
        }
    }
}

