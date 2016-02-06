package ru.turikhay.exceptions;

public class ParseException extends RuntimeException {
   public ParseException(String string) {
      super(string);
   }

   public ParseException(String message, Throwable cause) {
      super(message, cause);
   }
}
