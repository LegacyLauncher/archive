package ru.turikhay.tlauncher.adapter;

import ru.turikhay.tlauncher.ui.converter.StringConverter;

public class ClassAdapter extends AbstractClassAdapter {
   public ClassAdapter() {
      this.addDummyConverters();
   }

   public void addConverter(StringConverter converter) {
      super.addConverter(converter);
   }
}
