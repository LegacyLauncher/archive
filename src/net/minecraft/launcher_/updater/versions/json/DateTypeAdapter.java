package net.minecraft.launcher_.updater.versions.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
         Date date = this.deserializeToDate(json);
         if (typeOfT == Date.class) {
            return date;
         } else {
            throw new IllegalArgumentException(this.getClass() + " cannot deserialize to " + typeOfT);
         }
      }
   }

   public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
      synchronized(this.enUsFormat) {
         return new JsonPrimitive(this.serializeToString(src));
      }
   }

   private Date deserializeToDate(JsonElement json) {
      synchronized(this.enUsFormat) {
         Date var10000;
         try {
            var10000 = this.enUsFormat.parse(json.getAsString());
         } catch (ParseException var8) {
            try {
               var10000 = this.iso8601Format.parse(json.getAsString());
            } catch (ParseException var7) {
               try {
                  String cleaned = json.getAsString().replace("Z", "+00:00");
                  cleaned = cleaned.substring(0, 22) + cleaned.substring(23);
                  var10000 = this.iso8601Format.parse(cleaned);
               } catch (Exception var6) {
                  throw new JsonSyntaxException("Invalid date: " + json.getAsString(), var6);
               }

               return var10000;
            }

            return var10000;
         }

         return var10000;
      }
   }

   private String serializeToString(Date date) {
      synchronized(this.enUsFormat) {
         String result = this.iso8601Format.format(date);
         return result.substring(0, 22) + ":" + result.substring(22);
      }
   }
}
