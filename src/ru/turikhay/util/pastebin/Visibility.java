package ru.turikhay.util.pastebin;

public enum Visibility {
   PUBLIC(0),
   NOT_LISTED(1);

   private final int value;

   private Visibility(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }
}
