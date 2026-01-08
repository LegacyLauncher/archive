package net.minecraft.launcher.versions.json;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Locale;

public class LowerCaseEnumTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<?> rawType = type.getRawType();
        if (!rawType.isEnum())
            return null;

        final HashBiMap<String, Object> enumKeyMapping = HashBiMap.create();

        for (Object constant : rawType.getEnumConstants()) {
            String serializedName = serializedNameOf(constant, rawType);
            String key;
            if (serializedName != null) {
                key = serializedName;
            } else {
                key = nameOf(constant).toLowerCase(Locale.ROOT);
            }
            enumKeyMapping.put(key, constant);
        }

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, Object value) throws IOException {
                if (value == null)
                    out.nullValue();
                else
                    out.value(enumKeyMapping.inverse().get(value));
            }

            @Override
            @SuppressWarnings("unchecked")
            public T read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                }
                return (T) enumKeyMapping.get(reader.nextString());
            }
        };
    }

    private static String nameOf(Object constant) {
        return ((Enum<?>) constant).name();
    }

    @SneakyThrows
    private static String serializedNameOf(Object constant, Class<?> rawType) {
        Field field = rawType.getField(nameOf(constant));
        SerializedName annotation = field.getAnnotation(SerializedName.class);
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }
}
