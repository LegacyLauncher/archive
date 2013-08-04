package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.Map.Entry;

public final class TypeAdapters {
   public static final TypeAdapter CLASS = new TypeAdapter() {
      public void write(JsonWriter out, Class value) throws IOException {
         if (value == null) {
            out.nullValue();
         } else {
            throw new UnsupportedOperationException("Attempted to serialize java.lang.Class: " + value.getName() + ". Forgot to register a type adapter?");
         }
      }

      public Class read(JsonReader in) throws IOException {
         if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
         } else {
            throw new UnsupportedOperationException("Attempted to deserialize a java.lang.Class. Forgot to register a type adapter?");
         }
      }
   };
   public static final TypeAdapterFactory CLASS_FACTORY;
   public static final TypeAdapter BIT_SET;
   public static final TypeAdapterFactory BIT_SET_FACTORY;
   public static final TypeAdapter BOOLEAN;
   public static final TypeAdapter BOOLEAN_AS_STRING;
   public static final TypeAdapterFactory BOOLEAN_FACTORY;
   public static final TypeAdapter BYTE;
   public static final TypeAdapterFactory BYTE_FACTORY;
   public static final TypeAdapter SHORT;
   public static final TypeAdapterFactory SHORT_FACTORY;
   public static final TypeAdapter INTEGER;
   public static final TypeAdapterFactory INTEGER_FACTORY;
   public static final TypeAdapter LONG;
   public static final TypeAdapter FLOAT;
   public static final TypeAdapter DOUBLE;
   public static final TypeAdapter NUMBER;
   public static final TypeAdapterFactory NUMBER_FACTORY;
   public static final TypeAdapter CHARACTER;
   public static final TypeAdapterFactory CHARACTER_FACTORY;
   public static final TypeAdapter STRING;
   public static final TypeAdapter BIG_DECIMAL;
   public static final TypeAdapter BIG_INTEGER;
   public static final TypeAdapterFactory STRING_FACTORY;
   public static final TypeAdapter STRING_BUILDER;
   public static final TypeAdapterFactory STRING_BUILDER_FACTORY;
   public static final TypeAdapter STRING_BUFFER;
   public static final TypeAdapterFactory STRING_BUFFER_FACTORY;
   public static final TypeAdapter URL;
   public static final TypeAdapterFactory URL_FACTORY;
   public static final TypeAdapter URI;
   public static final TypeAdapterFactory URI_FACTORY;
   public static final TypeAdapter INET_ADDRESS;
   public static final TypeAdapterFactory INET_ADDRESS_FACTORY;
   public static final TypeAdapter UUID;
   public static final TypeAdapterFactory UUID_FACTORY;
   public static final TypeAdapterFactory TIMESTAMP_FACTORY;
   public static final TypeAdapter CALENDAR;
   public static final TypeAdapterFactory CALENDAR_FACTORY;
   public static final TypeAdapter LOCALE;
   public static final TypeAdapterFactory LOCALE_FACTORY;
   public static final TypeAdapter JSON_ELEMENT;
   public static final TypeAdapterFactory JSON_ELEMENT_FACTORY;
   public static final TypeAdapterFactory ENUM_FACTORY;

   private TypeAdapters() {
   }

   public static TypeAdapterFactory newEnumTypeHierarchyFactory() {
      return new TypeAdapterFactory() {
         public TypeAdapter create(Gson gson, TypeToken typeToken) {
            Class rawType = typeToken.getRawType();
            if (Enum.class.isAssignableFrom(rawType) && rawType != Enum.class) {
               if (!rawType.isEnum()) {
                  rawType = rawType.getSuperclass();
               }

               return new TypeAdapters.EnumTypeAdapter(rawType);
            } else {
               return null;
            }
         }
      };
   }

   public static TypeAdapterFactory newFactory(final TypeToken type, final TypeAdapter typeAdapter) {
      return new TypeAdapterFactory() {
         public TypeAdapter create(Gson gson, TypeToken typeToken) {
            return typeToken.equals(type) ? typeAdapter : null;
         }
      };
   }

   public static TypeAdapterFactory newFactory(final Class type, final TypeAdapter typeAdapter) {
      return new TypeAdapterFactory() {
         public TypeAdapter create(Gson gson, TypeToken typeToken) {
            return typeToken.getRawType() == type ? typeAdapter : null;
         }

         public String toString() {
            return "Factory[type=" + type.getName() + ",adapter=" + typeAdapter + "]";
         }
      };
   }

   public static TypeAdapterFactory newFactory(final Class unboxed, final Class boxed, final TypeAdapter typeAdapter) {
      return new TypeAdapterFactory() {
         public TypeAdapter create(Gson gson, TypeToken typeToken) {
            Class rawType = typeToken.getRawType();
            return rawType != unboxed && rawType != boxed ? null : typeAdapter;
         }

         public String toString() {
            return "Factory[type=" + boxed.getName() + "+" + unboxed.getName() + ",adapter=" + typeAdapter + "]";
         }
      };
   }

   public static TypeAdapterFactory newFactoryForMultipleTypes(final Class base, final Class sub, final TypeAdapter typeAdapter) {
      return new TypeAdapterFactory() {
         public TypeAdapter create(Gson gson, TypeToken typeToken) {
            Class rawType = typeToken.getRawType();
            return rawType != base && rawType != sub ? null : typeAdapter;
         }

         public String toString() {
            return "Factory[type=" + base.getName() + "+" + sub.getName() + ",adapter=" + typeAdapter + "]";
         }
      };
   }

   public static TypeAdapterFactory newTypeHierarchyFactory(final Class clazz, final TypeAdapter typeAdapter) {
      return new TypeAdapterFactory() {
         public TypeAdapter create(Gson gson, TypeToken typeToken) {
            return clazz.isAssignableFrom(typeToken.getRawType()) ? typeAdapter : null;
         }

         public String toString() {
            return "Factory[typeHierarchy=" + clazz.getName() + ",adapter=" + typeAdapter + "]";
         }
      };
   }

   static {
      CLASS_FACTORY = newFactory(Class.class, CLASS);
      BIT_SET = new TypeAdapter() {
         public BitSet read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               BitSet bitset = new BitSet();
               in.beginArray();
               int i = 0;

               for(JsonToken tokenType = in.peek(); tokenType != JsonToken.END_ARRAY; tokenType = in.peek()) {
                  boolean set;
                  switch(tokenType) {
                  case NUMBER:
                     set = in.nextInt() != 0;
                     break;
                  case BOOLEAN:
                     set = in.nextBoolean();
                     break;
                  case STRING:
                     String stringValue = in.nextString();

                     try {
                        set = Integer.parseInt(stringValue) != 0;
                        break;
                     } catch (NumberFormatException var8) {
                        throw new JsonSyntaxException("Error: Expecting: bitset number value (1, 0), Found: " + stringValue);
                     }
                  default:
                     throw new JsonSyntaxException("Invalid bitset value type: " + tokenType);
                  }

                  if (set) {
                     bitset.set(i);
                  }

                  ++i;
               }

               in.endArray();
               return bitset;
            }
         }

         public void write(JsonWriter out, BitSet src) throws IOException {
            if (src == null) {
               out.nullValue();
            } else {
               out.beginArray();

               for(int i = 0; i < src.length(); ++i) {
                  int value = src.get(i) ? 1 : 0;
                  out.value((long)value);
               }

               out.endArray();
            }
         }
      };
      BIT_SET_FACTORY = newFactory(BitSet.class, BIT_SET);
      BOOLEAN = new TypeAdapter() {
         public Boolean read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return in.peek() == JsonToken.STRING ? Boolean.parseBoolean(in.nextString()) : in.nextBoolean();
            }
         }

         public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
               out.nullValue();
            } else {
               out.value(value);
            }
         }
      };
      BOOLEAN_AS_STRING = new TypeAdapter() {
         public Boolean read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return Boolean.valueOf(in.nextString());
            }
         }

         public void write(JsonWriter out, Boolean value) throws IOException {
            out.value(value == null ? "null" : value.toString());
         }
      };
      BOOLEAN_FACTORY = newFactory(Boolean.TYPE, Boolean.class, BOOLEAN);
      BYTE = new TypeAdapter() {
         public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               try {
                  int intValue = in.nextInt();
                  return (byte)intValue;
               } catch (NumberFormatException var3) {
                  throw new JsonSyntaxException(var3);
               }
            }
         }

         public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
         }
      };
      BYTE_FACTORY = newFactory(Byte.TYPE, Byte.class, BYTE);
      SHORT = new TypeAdapter() {
         public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               try {
                  return (short)in.nextInt();
               } catch (NumberFormatException var3) {
                  throw new JsonSyntaxException(var3);
               }
            }
         }

         public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
         }
      };
      SHORT_FACTORY = newFactory(Short.TYPE, Short.class, SHORT);
      INTEGER = new TypeAdapter() {
         public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               try {
                  return in.nextInt();
               } catch (NumberFormatException var3) {
                  throw new JsonSyntaxException(var3);
               }
            }
         }

         public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
         }
      };
      INTEGER_FACTORY = newFactory(Integer.TYPE, Integer.class, INTEGER);
      LONG = new TypeAdapter() {
         public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               try {
                  return in.nextLong();
               } catch (NumberFormatException var3) {
                  throw new JsonSyntaxException(var3);
               }
            }
         }

         public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
         }
      };
      FLOAT = new TypeAdapter() {
         public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return (float)in.nextDouble();
            }
         }

         public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
         }
      };
      DOUBLE = new TypeAdapter() {
         public Number read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return in.nextDouble();
            }
         }

         public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
         }
      };
      NUMBER = new TypeAdapter() {
         public Number read(JsonReader in) throws IOException {
            JsonToken jsonToken = in.peek();
            switch(jsonToken) {
            case NUMBER:
               return new LazilyParsedNumber(in.nextString());
            case NULL:
               in.nextNull();
               return null;
            default:
               throw new JsonSyntaxException("Expecting number, got: " + jsonToken);
            }
         }

         public void write(JsonWriter out, Number value) throws IOException {
            out.value(value);
         }
      };
      NUMBER_FACTORY = newFactory(Number.class, NUMBER);
      CHARACTER = new TypeAdapter() {
         public Character read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               String str = in.nextString();
               if (str.length() != 1) {
                  throw new JsonSyntaxException("Expecting character, got: " + str);
               } else {
                  return str.charAt(0);
               }
            }
         }

         public void write(JsonWriter out, Character value) throws IOException {
            out.value(value == null ? null : String.valueOf(value));
         }
      };
      CHARACTER_FACTORY = newFactory(Character.TYPE, Character.class, CHARACTER);
      STRING = new TypeAdapter() {
         public String read(JsonReader in) throws IOException {
            JsonToken peek = in.peek();
            if (peek == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return peek == JsonToken.BOOLEAN ? Boolean.toString(in.nextBoolean()) : in.nextString();
            }
         }

         public void write(JsonWriter out, String value) throws IOException {
            out.value(value);
         }
      };
      BIG_DECIMAL = new TypeAdapter() {
         public BigDecimal read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               try {
                  return new BigDecimal(in.nextString());
               } catch (NumberFormatException var3) {
                  throw new JsonSyntaxException(var3);
               }
            }
         }

         public void write(JsonWriter out, BigDecimal value) throws IOException {
            out.value((Number)value);
         }
      };
      BIG_INTEGER = new TypeAdapter() {
         public BigInteger read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               try {
                  return new BigInteger(in.nextString());
               } catch (NumberFormatException var3) {
                  throw new JsonSyntaxException(var3);
               }
            }
         }

         public void write(JsonWriter out, BigInteger value) throws IOException {
            out.value((Number)value);
         }
      };
      STRING_FACTORY = newFactory(String.class, STRING);
      STRING_BUILDER = new TypeAdapter() {
         public StringBuilder read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return new StringBuilder(in.nextString());
            }
         }

         public void write(JsonWriter out, StringBuilder value) throws IOException {
            out.value(value == null ? null : value.toString());
         }
      };
      STRING_BUILDER_FACTORY = newFactory(StringBuilder.class, STRING_BUILDER);
      STRING_BUFFER = new TypeAdapter() {
         public StringBuffer read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return new StringBuffer(in.nextString());
            }
         }

         public void write(JsonWriter out, StringBuffer value) throws IOException {
            out.value(value == null ? null : value.toString());
         }
      };
      STRING_BUFFER_FACTORY = newFactory(StringBuffer.class, STRING_BUFFER);
      URL = new TypeAdapter() {
         public URL read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               String nextString = in.nextString();
               return "null".equals(nextString) ? null : new URL(nextString);
            }
         }

         public void write(JsonWriter out, URL value) throws IOException {
            out.value(value == null ? null : value.toExternalForm());
         }
      };
      URL_FACTORY = newFactory(URL.class, URL);
      URI = new TypeAdapter() {
         public URI read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               try {
                  String nextString = in.nextString();
                  return "null".equals(nextString) ? null : new URI(nextString);
               } catch (URISyntaxException var3) {
                  throw new JsonIOException(var3);
               }
            }
         }

         public void write(JsonWriter out, URI value) throws IOException {
            out.value(value == null ? null : value.toASCIIString());
         }
      };
      URI_FACTORY = newFactory(URI.class, URI);
      INET_ADDRESS = new TypeAdapter() {
         public InetAddress read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return InetAddress.getByName(in.nextString());
            }
         }

         public void write(JsonWriter out, InetAddress value) throws IOException {
            out.value(value == null ? null : value.getHostAddress());
         }
      };
      INET_ADDRESS_FACTORY = newTypeHierarchyFactory(InetAddress.class, INET_ADDRESS);
      UUID = new TypeAdapter() {
         public UUID read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               return java.util.UUID.fromString(in.nextString());
            }
         }

         public void write(JsonWriter out, UUID value) throws IOException {
            out.value(value == null ? null : value.toString());
         }
      };
      UUID_FACTORY = newFactory(UUID.class, UUID);
      TIMESTAMP_FACTORY = new TypeAdapterFactory() {
         public TypeAdapter create(Gson gson, TypeToken typeToken) {
            if (typeToken.getRawType() != Timestamp.class) {
               return null;
            } else {
               final TypeAdapter dateTypeAdapter = gson.getAdapter(Date.class);
               return new TypeAdapter() {
                  public Timestamp read(JsonReader in) throws IOException {
                     Date date = (Date)dateTypeAdapter.read(in);
                     return date != null ? new Timestamp(date.getTime()) : null;
                  }

                  public void write(JsonWriter out, Timestamp value) throws IOException {
                     dateTypeAdapter.write(out, value);
                  }
               };
            }
         }
      };
      CALENDAR = new TypeAdapter() {
         private static final String YEAR = "year";
         private static final String MONTH = "month";
         private static final String DAY_OF_MONTH = "dayOfMonth";
         private static final String HOUR_OF_DAY = "hourOfDay";
         private static final String MINUTE = "minute";
         private static final String SECOND = "second";

         public Calendar read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               in.beginObject();
               int year = 0;
               int month = 0;
               int dayOfMonth = 0;
               int hourOfDay = 0;
               int minute = 0;
               int second = 0;

               while(in.peek() != JsonToken.END_OBJECT) {
                  String name = in.nextName();
                  int value = in.nextInt();
                  if ("year".equals(name)) {
                     year = value;
                  } else if ("month".equals(name)) {
                     month = value;
                  } else if ("dayOfMonth".equals(name)) {
                     dayOfMonth = value;
                  } else if ("hourOfDay".equals(name)) {
                     hourOfDay = value;
                  } else if ("minute".equals(name)) {
                     minute = value;
                  } else if ("second".equals(name)) {
                     second = value;
                  }
               }

               in.endObject();
               return new GregorianCalendar(year, month, dayOfMonth, hourOfDay, minute, second);
            }
         }

         public void write(JsonWriter out, Calendar value) throws IOException {
            if (value == null) {
               out.nullValue();
            } else {
               out.beginObject();
               out.name("year");
               out.value((long)value.get(1));
               out.name("month");
               out.value((long)value.get(2));
               out.name("dayOfMonth");
               out.value((long)value.get(5));
               out.name("hourOfDay");
               out.value((long)value.get(11));
               out.name("minute");
               out.value((long)value.get(12));
               out.name("second");
               out.value((long)value.get(13));
               out.endObject();
            }
         }
      };
      CALENDAR_FACTORY = newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, CALENDAR);
      LOCALE = new TypeAdapter() {
         public Locale read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
               in.nextNull();
               return null;
            } else {
               String locale = in.nextString();
               StringTokenizer tokenizer = new StringTokenizer(locale, "_");
               String language = null;
               String country = null;
               String variant = null;
               if (tokenizer.hasMoreElements()) {
                  language = tokenizer.nextToken();
               }

               if (tokenizer.hasMoreElements()) {
                  country = tokenizer.nextToken();
               }

               if (tokenizer.hasMoreElements()) {
                  variant = tokenizer.nextToken();
               }

               if (country == null && variant == null) {
                  return new Locale(language);
               } else {
                  return variant == null ? new Locale(language, country) : new Locale(language, country, variant);
               }
            }
         }

         public void write(JsonWriter out, Locale value) throws IOException {
            out.value(value == null ? null : value.toString());
         }
      };
      LOCALE_FACTORY = newFactory(Locale.class, LOCALE);
      JSON_ELEMENT = new TypeAdapter() {
         public JsonElement read(JsonReader in) throws IOException {
            switch(in.peek()) {
            case NUMBER:
               String number = in.nextString();
               return new JsonPrimitive(new LazilyParsedNumber(number));
            case BOOLEAN:
               return new JsonPrimitive(in.nextBoolean());
            case STRING:
               return new JsonPrimitive(in.nextString());
            case NULL:
               in.nextNull();
               return JsonNull.INSTANCE;
            case BEGIN_ARRAY:
               JsonArray array = new JsonArray();
               in.beginArray();

               while(in.hasNext()) {
                  array.add(this.read(in));
               }

               in.endArray();
               return array;
            case BEGIN_OBJECT:
               JsonObject object = new JsonObject();
               in.beginObject();

               while(in.hasNext()) {
                  object.add(in.nextName(), this.read(in));
               }

               in.endObject();
               return object;
            case END_DOCUMENT:
            case NAME:
            case END_OBJECT:
            case END_ARRAY:
            default:
               throw new IllegalArgumentException();
            }
         }

         public void write(JsonWriter out, JsonElement value) throws IOException {
            if (value != null && !value.isJsonNull()) {
               if (value.isJsonPrimitive()) {
                  JsonPrimitive primitive = value.getAsJsonPrimitive();
                  if (primitive.isNumber()) {
                     out.value(primitive.getAsNumber());
                  } else if (primitive.isBoolean()) {
                     out.value(primitive.getAsBoolean());
                  } else {
                     out.value(primitive.getAsString());
                  }
               } else {
                  Iterator i$;
                  if (value.isJsonArray()) {
                     out.beginArray();
                     i$ = value.getAsJsonArray().iterator();

                     while(i$.hasNext()) {
                        JsonElement ex = (JsonElement)i$.next();
                        this.write(out, ex);
                     }

                     out.endArray();
                  } else {
                     if (!value.isJsonObject()) {
                        throw new IllegalArgumentException("Couldn't write " + value.getClass());
                     }

                     out.beginObject();
                     i$ = value.getAsJsonObject().entrySet().iterator();

                     while(i$.hasNext()) {
                        Entry e = (Entry)i$.next();
                        out.name((String)e.getKey());
                        this.write(out, (JsonElement)e.getValue());
                     }

                     out.endObject();
                  }
               }
            } else {
               out.nullValue();
            }

         }
      };
      JSON_ELEMENT_FACTORY = newTypeHierarchyFactory(JsonElement.class, JSON_ELEMENT);
      ENUM_FACTORY = newEnumTypeHierarchyFactory();
   }

   private static final class EnumTypeAdapter extends TypeAdapter {
      private final Map nameToConstant = new HashMap();
      private final Map constantToName = new HashMap();

      public EnumTypeAdapter(Class classOfT) {
         try {
            Enum[] arr$ = (Enum[])classOfT.getEnumConstants();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Enum constant = arr$[i$];
               String name = constant.name();
               SerializedName annotation = (SerializedName)classOfT.getField(name).getAnnotation(SerializedName.class);
               if (annotation != null) {
                  name = annotation.value();
               }

               this.nameToConstant.put(name, constant);
               this.constantToName.put(constant, name);
            }

         } catch (NoSuchFieldException var8) {
            throw new AssertionError();
         }
      }

      public Enum read(JsonReader in) throws IOException {
         if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
         } else {
            return (Enum)this.nameToConstant.get(in.nextString());
         }
      }

      public void write(JsonWriter out, Enum value) throws IOException {
         out.value(value == null ? null : (String)this.constantToName.get(value));
      }
   }
}
