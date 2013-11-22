package net.minecraft.launcher.updater.versions.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LowerCaseEnumTypeAdapterFactory implements TypeAdapterFactory {
   public TypeAdapter create(Gson gson, TypeToken type) {
      Class rawType = type.getRawType();
      if (!rawType.isEnum()) {
         return null;
      } else {
         final Map lowercaseToConstant = new HashMap();
         Object[] var8;
         int var7 = (var8 = rawType.getEnumConstants()).length;

         for(int var6 = 0; var6 < var7; ++var6) {
            Object constant = var8[var6];
            lowercaseToConstant.put(this.toLowercase(constant), constant);
         }

         return new TypeAdapter() {
            public void write(JsonWriter out, Object value) throws IOException {
               if (value == null) {
                  out.nullValue();
               } else {
                  out.value(LowerCaseEnumTypeAdapterFactory.this.toLowercase(value));
               }

            }

            public Object read(JsonReader reader) throws IOException {
               if (reader.peek() == JsonToken.NULL) {
                  reader.nextNull();
                  return null;
               } else {
                  return lowercaseToConstant.get(reader.nextString());
               }
            }
         };
      }
   }

   private String toLowercase(Object o) {
      return o.toString().toLowerCase(Locale.US);
   }
}
