package com.turikhay.tlauncher.configuration;

import com.turikhay.tlauncher.exceptions.ParseException;
import com.turikhay.util.IntegerArray;
import com.turikhay.util.StringUtil;
import java.io.File;

public class PlainParser {
   public static void parse(Object plainValue, Object defaultValue) throws ParseException {
      if (defaultValue != null) {
         if (plainValue == null) {
            throw new ParseException("Value is NULL");
         } else {
            String value = plainValue.toString();

            try {
               if (defaultValue instanceof Integer) {
                  Integer.parseInt(value);
               } else if (defaultValue instanceof Boolean) {
                  StringUtil.parseBoolean(value);
               } else if (defaultValue instanceof Double) {
                  Double.parseDouble(value);
               } else if (defaultValue instanceof Long) {
                  Long.parseLong(value);
               } else if (defaultValue instanceof IntegerArray) {
                  IntegerArray.parseIntegerArray(value);
               } else if (defaultValue instanceof Configuration.ActionOnLaunch) {
                  if (!Configuration.ActionOnLaunch.parse(value)) {
                     throw new ParseException("Cannot parse ActionOnLaunch");
                  }

                  if (defaultValue instanceof Configuration.ConsoleType) {
                     if (!Configuration.ConsoleType.parse(value)) {
                        throw new ParseException("Cannot parse ConsoleType");
                     }

                     if (defaultValue instanceof Configuration.ConnectionQuality) {
                        if (!Configuration.ConnectionQuality.parse(value)) {
                           throw new ParseException("Caanot parse ConnectionQuality");
                        }

                        if (defaultValue instanceof File) {
                           parseFile(value);
                        }
                     }
                  }
               }

            } catch (Exception var4) {
               if (var4 instanceof ParseException) {
                  throw (ParseException)var4;
               } else {
                  throw new ParseException("Cannot parse input value!", var4);
               }
            }
         }
      }
   }

   public static void parseFile(Object obj) throws ParseException {
      if (obj == null) {
         throw new ParseException("File is NULL");
      } else if (!(new File(obj.toString())).canRead()) {
         throw new ParseException("File is not accessible");
      }
   }
}
