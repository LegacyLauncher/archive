package com.turikhay.tlauncher.ui.converter;

public interface StringConverter {
   Object fromString(String var1);

   String toString(Object var1);

   String toValue(Object var1);
}
