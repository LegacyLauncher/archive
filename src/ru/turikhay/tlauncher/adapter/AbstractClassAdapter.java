package ru.turikhay.tlauncher.adapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ru.turikhay.tlauncher.ui.converter.StringConverter;
import ru.turikhay.tlauncher.ui.converter.dummy.DummyConverter;
import ru.turikhay.util.U;

public abstract class AbstractClassAdapter {
   protected final List values = new ArrayList();
   protected final List converters = new ArrayList();
   protected PublicCloneable initial;

   protected AbstractClassAdapter() {
   }

   public PublicCloneable getInitial() {
      return this.initial;
   }

   public void setInitial(PublicCloneable instance) {
      this.initial = instance;
   }

   protected void addConverter(StringConverter converter) {
      if (converter == null) {
         throw new NullPointerException();
      } else {
         this.converters.add(converter);
      }
   }

   protected void addConverters(StringConverter[] converters) {
      for(int i = 0; i < converters.length; ++i) {
         StringConverter converter = converters[i];
         if (converter == null) {
            throw new NullPointerException("StringConverter at " + i + " is NULL!");
         }

         this.converters.add(converter);
      }

   }

   protected void removeConverter(StringConverter converter) {
      if (converter == null) {
         throw new NullPointerException();
      } else {
         this.converters.remove(converter);
      }
   }

   protected List getConverters() {
      return this.converters;
   }

   public synchronized void refreshValues() throws ClassAdapterException {
      if (this.initial == null) {
         throw new NullPointerException("Initial instance is not defined!");
      } else {
         ArrayList list = new ArrayList();
         Field[] fields = this.initial.getClass().getDeclaredFields();
         Field[] var6 = fields;
         int var5 = fields.length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Field field = var6[var4];
            Class fieldClass = field.getType();
            StringConverter converter = null;
            Iterator var10 = this.converters.iterator();

            while(var10.hasNext()) {
               StringConverter checkConverter = (StringConverter)var10.next();
               if (checkConverter.getObjectClass().equals(fieldClass)) {
                  this.log("Found converter for field:", field);
                  converter = checkConverter;
                  break;
               }
            }

            if (converter == null) {
               this.log("Cannot find coverter for field:", field, fieldClass);
            } else {
               try {
                  field.setAccessible(true);
               } catch (RuntimeException var14) {
                  throw new ClassAdapterException("Field cannot be set accessible: " + field, var14);
               }

               String fieldName = field.getName();

               Object fieldValue;
               try {
                  fieldValue = field.get(this.initial);
               } catch (Exception var13) {
                  throw new ClassAdapterException("Cannot get field value: " + field, var13);
               }

               list.add(new AdaptedValue(this.initial, fieldName, fieldValue, converter));

               try {
                  field.setAccessible(false);
               } catch (RuntimeException var12) {
                  this.log("Cannot set default accessibility for field:", field);
               }
            }
         }

         this.values.clear();
         this.values.addAll(list);
      }
   }

   public synchronized PublicCloneable createInstance() throws ClassAdapterException {
      if (this.initial == null) {
         throw new NullPointerException("Initial instance is not defined!");
      } else {
         PublicCloneable instance;
         try {
            instance = (PublicCloneable)this.initial.cloneSafely();
            if (instance == null) {
               throw new NullPointerException("New instance is NULL!");
            }
         } catch (Exception var14) {
            throw new ClassAdapterException("Cannot create clone instance!", var14);
         }

         Field[] fields = instance.getClass().getDeclaredFields();
         Iterator var4 = this.values.iterator();

         while(var4.hasNext()) {
            AdaptedValue adapted = (AdaptedValue)var4.next();
            String fieldName = adapted.getKey();
            Field field = null;
            Field[] var10 = fields;
            int var9 = fields.length;

            for(int var8 = 0; var8 < var9; ++var8) {
               Field checkField = var10[var8];
               if (fieldName.equals(checkField.getName())) {
                  field = checkField;
                  break;
               }
            }

            if (field == null) {
               this.log("No value for field:", fieldName);
            } else {
               try {
                  field.setAccessible(true);
               } catch (RuntimeException var13) {
                  throw new ClassAdapterException("Field cannot be set accessible: " + field, var13);
               }

               Object fieldValue = adapted.getValue();
               this.log("Setting:", fieldName, fieldValue);

               try {
                  field.set(instance, fieldValue);
               } catch (Exception var12) {
                  throw new ClassAdapterException("Field cannot be set: " + field + " = " + fieldValue, var12);
               }

               try {
                  field.setAccessible(false);
               } catch (RuntimeException var11) {
                  this.log("Cannot set default accessibility for field:", field);
               }
            }
         }

         return instance;
      }
   }

   protected void addDummyConverters() {
      this.addConverters(DummyConverter.getConverters());
   }

   protected void log(Object... o) {
      U.log("[" + this.getClass().getSimpleName() + "]", o);
   }
}
