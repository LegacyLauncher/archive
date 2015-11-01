package ru.turikhay.tlauncher.ui.converter;

import ru.turikhay.tlauncher.managers.ProfileManager;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

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
      return from == null ? Localizable.get("account.empty") : (from.getUsername() == null ? null : from.getUsername());
   }

   public Account fromString(String from) {
      return this.pm.getAuthDatabase().getByUsername(from, (Account.AccountType)null);
   }

   public String toValue(Account from) {
      return from != null && from.getUsername() != null ? from.getUsername() : null;
   }

   public String toPath(Account from) {
      return null;
   }

   public Class getObjectClass() {
      return Account.class;
   }
}
