package ru.turikhay.tlauncher.updater;

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
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.util.IntegerArray;

public class Ads {
   private final Map map = new HashMap();
   private final Map unmodifiable;

   public Ads() {
      this.unmodifiable = Collections.unmodifiableMap(this.map);
   }

   public final Map getMap() {
      return this.unmodifiable;
   }

   protected final Map map() {
      return this.map;
   }

   public final Ads.AdList getByName(String name) {
      return (Ads.AdList)this.map.get(name);
   }

   protected void add(Ads.AdList list) {
      if (list == null) {
         throw new NullPointerException("list");
      } else {
         this.map.put(list.name, list);
      }
   }

   protected void add(String listName, Ads.Ad ad) {
      if (ad == null) {
         throw new NullPointerException("ad");
      } else {
         Ads.AdList list = (Ads.AdList)this.map.get(listName);
         boolean add = list == null;
         if (add) {
            list = new Ads.AdList(listName);
         }

         list.add(ad);
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
         URL url = ImageCache.getRes(image);
         return url == null ? null : url.toString();
      }
   }

   public static class Ad {
      private int chance;
      private String content;
      private int[] size = new int[2];
      private String image;

      public Ad(int chance, String content, int[] size, String image) {
         this.setChance(chance);
         this.setContent(content);
         this.setSize(size);
         this.setImage(image);
      }

      public final int getChance() {
         return this.chance;
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

      public final int[] getSize() {
         return (int[])this.size.clone();
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
         this.image = StringUtils.isBlank(image) ? null : Ads.parseImage(image);
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

   public static class AdList {
      private final String name;
      private final List list = new ArrayList();
      private final List unmodifiable;
      private final Ads.Ad[] chances;
      private int totalChance;

      public AdList(String name) {
         this.unmodifiable = Collections.unmodifiableList(this.list);
         this.chances = new Ads.Ad[100];
         this.totalChance = 0;
         if (name == null) {
            throw new NullPointerException("name");
         } else if (name.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
         } else {
            this.name = name;
         }
      }

      public final String getName() {
         return this.name;
      }

      public final List getAds() {
         return this.unmodifiable;
      }

      protected final List list() {
         return this.list;
      }

      public final Ads.Ad getRandom() {
         return this.chances[(new Random()).nextInt(100)];
      }

      protected void add(Ads.Ad ad) {
         if (ad == null) {
            throw new NullPointerException();
         } else if (this.totalChance + ad.chance > 100) {
            throw new IllegalArgumentException("chance overflow: " + (this.totalChance + ad.chance));
         } else {
            this.list.add(ad);
            Arrays.fill(this.chances, this.totalChance, this.totalChance + ad.chance, ad);
            this.totalChance += ad.chance;
         }
      }

      public String toString() {
         return this.getClass().getSimpleName() + this.list();
      }
   }

   public static class Deserializer implements JsonDeserializer {
      public Ads deserialize(JsonElement root, Type type, JsonDeserializationContext context) throws JsonParseException {
         Ads ads = new Ads();
         JsonObject rootObject = root.getAsJsonObject();
         Iterator var7 = rootObject.entrySet().iterator();

         while(var7.hasNext()) {
            Entry entry = (Entry)var7.next();
            String listName = (String)entry.getKey();
            JsonObject adObj = ((JsonElement)entry.getValue()).getAsJsonObject();
            ads.add(listName, new Ads.Ad(adObj.has("chance") ? adObj.get("chance").getAsInt() : 100, adObj.get("content").getAsString(), IntegerArray.parseIntegerArray(adObj.get("size").getAsString(), 'x').toArray(), adObj.has("image") ? adObj.get("image").getAsString() : null));
         }

         return ads;
      }
   }
}
