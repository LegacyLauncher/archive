package org.apache.commons.lang3;

public class Validate
{
  public static <T extends CharSequence> T notBlank(T chars, String message, Object[] values)
  {
    if (chars == null) {
      throw new NullPointerException(String.format(message, values));
    }
    if (StringUtils.isBlank(chars)) {
      throw new IllegalArgumentException(String.format(message, values));
    }
    return chars;
  }

  public static <T extends CharSequence> T notBlank(T chars)
  {
    return notBlank(chars, "The validated character sequence is blank", new Object[0]);
  }
}