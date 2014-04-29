package ru.turikhay.tlauncher.ui.converter.dummy;

import java.util.Date;
import net.minecraft.launcher.versions.json.DateTypeAdapter;

public class DummyDateConverter extends DummyConverter {
   private final DateTypeAdapter dateAdapter = new DateTypeAdapter();

   public Date fromDummyString(String from) throws RuntimeException {
      return this.dateAdapter.toDate(from);
   }

   public String toDummyValue(Date value) throws RuntimeException {
      return this.dateAdapter.toString(value);
   }

   public Class getObjectClass() {
      return Date.class;
   }
}
