package org.apache.commons.lang3;

import java.io.Serializable;

public class ObjectUtils
{
  public static final Null NULL = new Null();

  public static String toString(Object obj)
  {
    return obj == null ? "" : obj.toString();
  }

  @SuppressWarnings("serial")
public static class Null
    implements Serializable
  {
  }
}