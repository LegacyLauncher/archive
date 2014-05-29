package ru.turikhay.tlauncher.minecraft.auth;

import java.util.LinkedHashMap;

class RawUserProperty extends LinkedHashMap {
   private static final long serialVersionUID = 1281494999913983388L;

   public UserProperty toProperty() {
      return new UserProperty((String)this.get("name"), (String)this.get("value"));
   }
}
