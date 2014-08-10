package ru.turikhay.tlauncher.updater;

import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import ru.turikhay.exceptions.ParseException;
import ru.turikhay.tlauncher.configuration.SimpleConfiguration;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.util.IntegerArray;
import ru.turikhay.util.Reflect;
import ru.turikhay.util.U;

public class AdParser {
   public static final int SIZE_ARR_LENGTH = 2;
   private static final char KEY_DELIMITER = '.';
   private static final char VALUE_DELIMITER = '|';
   private static final char ESCAPE = '\\';
   private static final char SIZE_DELIMITER = 'x';
   private static final String AD_PREFIX = "ad.";
   private static final String BASE64_IND = "data:image";
   private static final String COPY_IND = "copy:";
   private final Map ads;

   private AdParser() {
      this.ads = new Hashtable();
   }

   private AdParser(SimpleConfiguration c) throws RuntimeException {
      this();
      if (c == null) {
         throw new NullPointerException("configuration");
      } else {
         Iterator var3 = c.getKeys().iterator();

         while(true) {
            String key;
            do {
               if (!var3.hasNext()) {
                  var3 = this.ads.values().iterator();

                  while(var3.hasNext()) {
                     AdParser.Ad ad = (AdParser.Ad)var3.next();

                     String imageCopy;
                     try {
                        imageCopy = needCopy(ad.getImage());
                     } catch (RuntimeException var6) {
                        continue;
                     }

                     if (imageCopy != null) {
                        AdParser.Ad copyAd = (AdParser.Ad)this.ads.get(imageCopy);
                        if (copyAd == null) {
                           log("Cannot find copy ad indicated by image in", ad);
                        } else {
                           ad.setImage(copyAd.getImage());
                        }
                     }
                  }

                  return;
               }

               key = (String)var3.next();
            } while(!key.startsWith("ad."));

            AdParser.Ad ad;
            try {
               ad = this.parsePair(key, c.get(key));
            } catch (RuntimeException var7) {
               log("Error parsing key:", key, var7);
               continue;
            }

            if (ad == null) {
               throw new RuntimeException("Well... what the...? (" + key + ")");
            }

            this.ads.put(ad.getLocale(), ad);
         }
      }
   }

   public AdParser.Ad get(String locale) {
      return (AdParser.Ad)this.ads.get(locale);
   }

   private AdParser.Ad parsePair(String key, String value) throws RuntimeException {
      String parsing = key.substring("ad.".length());
      if (parsing.isEmpty()) {
         throw new IllegalArgumentException("cannot determine locale");
      } else {
         String locale = seek(parsing, 0, '.');
         String tempType = seek(value, 0);

         AdParser.AdType type;
         try {
            type = (AdParser.AdType)Reflect.parseEnum0(AdParser.AdType.class, tempType);
         } catch (ParseException var15) {
            type = null;
         }

         int caret = tempType.length();
         String content;
         if (type == null) {
            content = tempType;
            type = AdParser.AdType.DEFAULT;
         } else {
            content = seek(value, caret);
            caret += content.length();
         }

         ++caret;
         String tempSize = seek(value, caret);

         IntegerArray sizeArray;
         try {
            sizeArray = IntegerArray.parseIntegerArray(tempSize, 'x');
         } catch (RuntimeException var14) {
            throw new ParseException("Cannot parse size: \"" + tempSize + "\"", var14);
         }

         if (sizeArray.size() != 2) {
            throw new IllegalArgumentException("illegal size array length");
         } else {
            int[] size = sizeArray.toArray();
            caret += tempSize.length();
            ++caret;
            String tempImage = seek(value, caret);
            String image = tempImage.isEmpty() ? null : parseImage(tempImage);
            return new AdParser.Ad(locale, type, content, size, image, (AdParser.Ad)null);
         }
      }
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.getClass().getSimpleName()).append("{ads=").append(U.toLog(this.ads)).append('}');
      return builder.toString();
   }

   private static String seek(String parsing, int caret, char delimiter) {
      StringBuilder builder = new StringBuilder();

      char c;
      for(char lastchar = 0; caret < parsing.length(); lastchar = c) {
         c = parsing.charAt(caret);
         ++caret;
         if (c == delimiter && lastchar != '\\') {
            break;
         }

         builder.append(c);
      }

      return builder.toString();
   }

   private static String seek(String parsing, int caret) {
      return seek(parsing, caret, '|');
   }

   private static String needCopy(String value) {
      if (value.startsWith("copy:")) {
         String valueKey = value.substring("copy:".length());
         if (valueKey.isEmpty()) {
            throw new IllegalArgumentException("copy key is null");
         } else {
            return valueKey;
         }
      } else {
         return null;
      }
   }

   static AdParser parseFrom(SimpleConfiguration configuration) {
      try {
         return new AdParser(configuration);
      } catch (RuntimeException var2) {
         U.log(var2);
         return null;
      }
   }

   static String parseImage(String image) {
      if (image == null) {
         return null;
      } else if (!image.startsWith("data:image") && !image.startsWith("copy:")) {
         URL url = ImageCache.getRes(image);
         return url == null ? null : url.toString();
      } else {
         return image;
      }
   }

   private static void log(Object... o) {
      U.log("[AdParser]", o);
   }

   public class Ad {
      private final String locale;
      private final AdParser.AdType type;
      private final String content;
      private final int[] size;
      private String image;

      private Ad(String locale, AdParser.AdType type, String content, int[] size, String image) {
         if (locale == null) {
            throw new NullPointerException("locale");
         } else if (locale.isEmpty()) {
            throw new IllegalArgumentException("locale is empty");
         } else if (type == null) {
            throw new NullPointerException("type");
         } else if (content == null) {
            throw new NullPointerException("content");
         } else if (content.isEmpty()) {
            throw new IllegalArgumentException("content is empty");
         } else if (size == null) {
            throw new NullPointerException("size");
         } else if (size.length != 2) {
            throw new IllegalArgumentException("size array has illegal length:" + size.length + " (instead of " + 2 + ")");
         } else {
            this.locale = locale;
            this.type = type;
            this.content = content;
            this.size = size;
            this.image = image;
         }
      }

      public String getLocale() {
         return this.locale;
      }

      public AdParser.AdType getType() {
         return this.type;
      }

      public String getContent() {
         return this.content;
      }

      public String getImage() {
         return this.image;
      }

      void setImage(String image) {
         this.image = image;
      }

      public int getWidth() {
         return this.size[0];
      }

      public int getHeight() {
         return this.size[1];
      }

      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append(this.getClass().getSimpleName()).append("{").append("locale=").append(this.locale).append(';').append("type=").append(this.type).append(';').append("size=").append(this.size[0]).append('x').append(this.size[1]).append(';').append("content=\"");
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

      // $FF: synthetic method
      Ad(String var2, AdParser.AdType var3, String var4, int[] var5, String var6, AdParser.Ad var7) {
         this(var2, var3, var4, var5, var6);
      }
   }

   public static enum AdType {
      DEFAULT;
   }
}
