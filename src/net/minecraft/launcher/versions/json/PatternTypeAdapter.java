package net.minecraft.launcher.versions.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class PatternTypeAdapter implements JsonDeserializer, JsonSerializer {
   public Pattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      String string = json.getAsString();
      return StringUtils.isBlank(string) ? null : Pattern.compile(string);
   }

   public JsonElement serialize(Pattern src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src == null ? null : src.toString());
   }
}
