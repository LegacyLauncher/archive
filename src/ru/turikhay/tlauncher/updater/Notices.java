package ru.turikhay.tlauncher.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class Notices {
   private final Map map = new HashMap();
   private final Map unmodifiable;

   public Notices() {
      this.unmodifiable = Collections.unmodifiableMap(this.map);
   }

   public final Notices.NoticeList getByName(String name) {
      return (Notices.NoticeList)this.map.get(name);
   }

   protected void add(Notices.NoticeList list) {
      if (list == null) {
         throw new NullPointerException("list");
      } else {
         this.map.put(list.name, list);
      }
   }

   protected void add(String listName, Notices.Notice notice) {
      if (notice == null) {
         throw new NullPointerException("notice");
      } else {
         Notices.NoticeList list = (Notices.NoticeList)this.map.get(listName);
         boolean add = list == null;
         if (add) {
            list = new Notices.NoticeList(listName);
         }

         list.add(notice);
         if (add) {
            this.add(list);
         }

      }
   }

   public String toString() {
      return this.getClass().getSimpleName() + this.map;
   }

   private static String parseImage(String image) {
      if (image == null) {
         return null;
      } else if (image.startsWith("data:image")) {
         return image;
      } else {
         URL url = Images.getRes(image, false);
         return url == null ? null : url.toString();
      }
   }

   public static enum NoticeType {
      NOTICE(false),
      WARNING(false),
      AD_SERVER,
      AD_YOUTUBE,
      AD_OTHER;

      private final boolean advert;

      private NoticeType(boolean advert) {
         this.advert = advert;
      }

      private NoticeType() {
         this(true);
      }

      public boolean isAdvert() {
         return this.advert;
      }
   }

   public static class NoticeList {
      private final String name;
      private final List list = new ArrayList();
      private final List unmodifiable;
      private final Notices.Notice[] chances;
      private int totalChance;

      public NoticeList(String name) {
         this.unmodifiable = Collections.unmodifiableList(this.list);
         this.chances = new Notices.Notice[100];
         this.totalChance = 0;
         if (name == null) {
            throw new NullPointerException("name");
         } else if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
         } else {
            this.name = name;
         }
      }

      public final List getList() {
         return this.unmodifiable;
      }

      protected final List list() {
         return this.list;
      }

      public final Notices.Notice getRandom() {
         return this.chances[(new Random()).nextInt(100)];
      }

      protected void add(Notices.Notice notice) {
         if (notice == null) {
            throw new NullPointerException();
         } else if (this.totalChance + notice.chance > 100) {
            throw new IllegalArgumentException("chance overflow: " + (this.totalChance + notice.chance));
         } else {
            this.list.add(notice);
            Arrays.fill(this.chances, this.totalChance, this.totalChance + notice.chance, notice);
            this.totalChance += notice.chance;
         }
      }

      public String toString() {
         return this.getClass().getSimpleName() + this.list();
      }
   }

   public static class Notice {
      private String content;
      private int id;
      private int chance = 100;
      private Notices.NoticeType type;
      private int[] size;
      private String image;

      public Notice() {
         this.type = Notices.NoticeType.NOTICE;
         this.size = new int[2];
      }

      public final int getId() {
         return this.id;
      }

      public final void setId(int id) {
         this.id = id;
      }

      public final void setChance(int chance) {
         if (chance >= 1 && chance <= 100) {
            this.chance = chance;
         } else {
            throw new IllegalArgumentException("illegal chance: " + chance);
         }
      }

      public final String getContent() {
         return this.content;
      }

      public final void setContent(String content) {
         if (StringUtils.isBlank(content)) {
            throw new IllegalArgumentException("content is empty or is null");
         } else {
            this.content = content;
         }
      }

      public final Notices.NoticeType getType() {
         return this.type;
      }

      public final void setType(Notices.NoticeType type) {
         this.type = type;
      }

      public final void setSize(int[] size) {
         if (size == null) {
            throw new NullPointerException();
         } else if (size.length != 2) {
            throw new IllegalArgumentException("illegal length");
         } else {
            this.setWidth(size[0]);
            this.setHeight(size[1]);
         }
      }

      public final int getWidth() {
         return this.size[0];
      }

      public final void setWidth(int width) {
         if (width < 1) {
            throw new IllegalArgumentException("width must be greater than 0");
         } else {
            this.size[0] = width;
         }
      }

      public final int getHeight() {
         return this.size[1];
      }

      public final void setHeight(int height) {
         if (height < 1) {
            throw new IllegalArgumentException("height must be greater than 0");
         } else {
            this.size[1] = height;
         }
      }

      public final String getImage() {
         return this.image;
      }

      public final void setImage(String image) {
         this.image = StringUtils.isBlank(image) ? null : Notices.parseImage(image);
      }

      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append(this.getClass().getSimpleName()).append("{").append("size=").append(this.size[0]).append('x').append(this.size[1]).append(';').append("chance=").append(this.chance).append(';').append("content=\"");
         if (this.content.length() < 50) {
            builder.append(this.content);
         } else {
            builder.append(this.content.substring(0, 46)).append("...");
         }

         builder.append("\";").append("image=");
         if (this.image != null && this.image.length() > 24) {
            builder.append(this.image.substring(0, 22)).append("...");
         } else {
            builder.append(this.image);
         }

         builder.append('}');
         return builder.toString();
      }
   }

   public static class Deserializer implements JsonDeserializer {
      public Notices deserialize(JsonElement root, Type type, JsonDeserializationContext context) throws JsonParseException {
         try {
            return this.deserialize0(root);
         } catch (Exception var5) {
            U.log("Cannot parse notices:", var5);
            return new Notices();
         }
      }

      private Notices deserialize0(JsonElement root) throws JsonParseException {
         Notices notices = new Notices();
         JsonObject rootObject = root.getAsJsonObject();
         Iterator var5 = rootObject.entrySet().iterator();

         Notices.Notice notice3;
         label85:
         while(var5.hasNext()) {
            Entry notice = (Entry)var5.next();
            String listName = (String)notice.getKey();
            JsonArray ntArray = ((JsonElement)notice.getValue()).getAsJsonArray();
            Iterator var9 = ntArray.iterator();

            while(true) {
               JsonObject ntObj;
               Pattern pattern;
               do {
                  if (!var9.hasNext()) {
                     continue label85;
                  }

                  JsonElement elem = (JsonElement)var9.next();
                  ntObj = elem.getAsJsonObject();
                  if (!ntObj.has("version")) {
                     break;
                  }

                  String notice1 = ntObj.get("version").getAsString();
                  pattern = Pattern.compile(notice1);
               } while(!pattern.matcher(String.valueOf(TLauncher.getVersion())).matches());

               notice3 = new Notices.Notice();
               notice3.setContent(ntObj.get("content").getAsString());
               notice3.setSize(IntegerArray.parseIntegerArray(ntObj.get("size").getAsString(), 'x').toArray());
               if (ntObj.has("id")) {
                  notice3.setId(ntObj.get("id").getAsInt());
               }

               if (ntObj.has("chance")) {
                  notice3.setChance(ntObj.get("chance").getAsInt());
               }

               if (ntObj.has("type")) {
                  notice3.setType((Notices.NoticeType)Reflect.parseEnum(Notices.NoticeType.class, ntObj.get("type").getAsString()));
               }

               if (ntObj.has("image")) {
                  notice3.setImage(ntObj.get("image").getAsString());
               }

               notices.add(listName, notice3);
            }
         }

         if (!TLauncher.getBrand().equals("Legacy") && notices.getByName(Locale.US.toString()) != null) {
            List universalList = notices.getByName(Locale.US.toString()).getList();
            Locale[] var15 = TLauncher.getInstance().getLang().getLocales();
            int var16 = var15.length;

            for(int var17 = 0; var17 < var16; ++var17) {
               Locale locale = var15[var17];
               if (!locale.equals(Locale.US) && notices.getByName(locale.toString()) == null) {
                  Iterator var19 = universalList.iterator();

                  while(var19.hasNext()) {
                     notice3 = (Notices.Notice)var19.next();
                     notices.add(locale.toString(), notice3);
                  }
               }
            }
         }

         if (notices.getByName("uk_UA") == null && notices.getByName("ru_RU") != null) {
            var5 = notices.getByName("ru_RU").getList().iterator();

            while(var5.hasNext()) {
               Notices.Notice notice2 = (Notices.Notice)var5.next();
               notices.add("uk_UA", notice2);
            }
         }

         return notices;
      }
   }
}
