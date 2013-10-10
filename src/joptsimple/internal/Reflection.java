package joptsimple.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import joptsimple.ValueConverter;

public final class Reflection {
   private Reflection() {
   }

   public static ValueConverter findConverter(Class clazz) {
      ValueConverter valueOf = valueOfConverter(clazz);
      if (valueOf != null) {
         return valueOf;
      } else {
         ValueConverter constructor = constructorConverter(clazz);
         if (constructor != null) {
            return constructor;
         } else {
            throw new IllegalArgumentException(clazz + " is not a value type");
         }
      }
   }

   private static ValueConverter valueOfConverter(Class clazz) {
      try {
         Method valueOf = clazz.getDeclaredMethod("valueOf", String.class);
         return !meetsConverterRequirements(valueOf, clazz) ? null : new MethodInvokingValueConverter(valueOf, clazz);
      } catch (NoSuchMethodException var2) {
         return null;
      }
   }

   private static ValueConverter constructorConverter(Class clazz) {
      try {
         return new ConstructorInvokingValueConverter(clazz.getConstructor(String.class));
      } catch (NoSuchMethodException var2) {
         return null;
      }
   }

   public static Object instantiate(Constructor constructor, Object... args) {
      try {
         return constructor.newInstance(args);
      } catch (Exception var3) {
         throw reflectionException(var3);
      }
   }

   public static Object invoke(Method method, Object... args) {
      try {
         return method.invoke((Object)null, args);
      } catch (Exception var3) {
         throw reflectionException(var3);
      }
   }

   private static boolean meetsConverterRequirements(Method method, Class expectedReturnType) {
      int modifiers = method.getModifiers();
      return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && expectedReturnType.equals(method.getReturnType());
   }

   private static RuntimeException reflectionException(Exception ex) {
      if (ex instanceof IllegalArgumentException) {
         return new ReflectionException(ex);
      } else if (ex instanceof InvocationTargetException) {
         return new ReflectionException(ex.getCause());
      } else {
         return (RuntimeException)(ex instanceof RuntimeException ? (RuntimeException)ex : new ReflectionException(ex));
      }
   }

   static {
      new Reflection();
   }
}
