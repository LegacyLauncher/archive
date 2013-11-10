package com.turikhay.tlauncher.ui;

public class TimeoutField extends LocalizableTextField implements SettingsField {
   private static final long serialVersionUID = -1540285891285378219L;
   private TimeoutField.FieldType ft;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$TimeoutField$FieldType;

   TimeoutField(SettingsForm sf, TimeoutField.FieldType ft) {
      this.ft = ft;
      this.addFocusListener(sf.warner);
   }

   protected boolean check(String text) {
      boolean var2 = true;

      int cur;
      try {
         cur = Integer.parseInt(text);
      } catch (Exception var4) {
         return this.setError(l.get("settings.timeouts.incorrect"));
      }

      return cur < 1 ? this.setError(l.get("settings.timeouts.incorrect")) : true;
   }

   public String getSettingsPath() {
      switch($SWITCH_TABLE$com$turikhay$tlauncher$ui$TimeoutField$FieldType()[this.ft.ordinal()]) {
      case 1:
         return "timeout.read";
      case 2:
         return "timeout.connection";
      default:
         throw new IllegalStateException("Unknown field type!");
      }
   }

   public boolean isValueValid() {
      return this.check();
   }

   public void setToDefault() {
      this.setValue(15000);
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$turikhay$tlauncher$ui$TimeoutField$FieldType() {
      int[] var10000 = $SWITCH_TABLE$com$turikhay$tlauncher$ui$TimeoutField$FieldType;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[TimeoutField.FieldType.values().length];

         try {
            var0[TimeoutField.FieldType.CONNECTION.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[TimeoutField.FieldType.READ.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$turikhay$tlauncher$ui$TimeoutField$FieldType = var0;
         return var0;
      }
   }

   public static enum FieldType {
      READ,
      CONNECTION;
   }
}
