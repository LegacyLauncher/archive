package com.turikhay.tlauncher.ui.converter;

import com.turikhay.tlauncher.managers.ProfileManager;
import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class AccountConverter extends LocalizableStringConverter {
   private final ProfileManager pm;

   public AccountConverter(ProfileManager pm) {
      super((String)null);
      if (pm == null) {
         throw new NullPointerException();
      } else {
         this.pm = pm;
      }
   }

   public String toString(Account from) {
      if (from == null) {
         return Localizable.get("account.empty");
      } else {
         return from.getUsername() == null ? null : from.getUsername();
      }
   }

   public Account fromString(String from) {
      return this.pm.getAuthDatabase().getByUsername(from);
   }

   public String toValue(Account from) {
      return from != null && from.getUsername() != null ? from.getUsername() : null;
   }

   public String toPath(Account from) {
      return null;
   }
}
