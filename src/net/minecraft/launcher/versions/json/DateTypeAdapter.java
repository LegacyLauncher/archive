package net.minecraft.launcher.versions.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import ru.turikhay.exceptions.ParseException;

public class DateTypeAdapter implements JsonDeserializer, JsonSerializer {
   private final DateFormat enUsFormat;
   private final DateFormat iso8601Format;

   public DateTypeAdapter() {
      this.enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
      this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
   }

   public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      if (!(json instanceof JsonPrimitive)) {
         throw new JsonParseException("The date should be a string value");
      } else {
         Date date = this.toDate(json.getAsString());
         if (typeOfT == Date.class) {
            return date;
         } else {
            throw new IllegalArgumentException(this.getClass() + " cannot deserialize to " + typeOfT);
         }
      }
   }

   public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
      synchronized(this.enUsFormat) {
         return new JsonPrimitive(this.toString(src));
      }
   }

   public String toString(Date date) {
      synchronized(this.enUsFormat) {
         String result = this.iso8601Format.format(date);
         return result.substring(0, 22) + ":" + result.substring(22);
      }
   }

   public Date toDate(String string) {
      synchronized(this.enUsFormat) {
         Date var10000;
         try {
            var10000 = this.enUsFormat.parse(string);
         } catch (Exception var6) {
            try {
               var10000 = this.iso8601Format.parse(string);
            } catch (Exception var5) {
               try {
                  String cleaned = string.replace("Z", "+00:00");
                  cleaned = cleaned.substring(0, 22) + cleaned.substring(23);
                  var10000 = this.iso8601Format.parse(cleaned);
               } catch (Exception var4) {
                  throw new ParseException("Invalid date: " + string, var4);
               }

               return var10000;
            }

            return var10000;
         }

         return var10000;
      }
   }
}
