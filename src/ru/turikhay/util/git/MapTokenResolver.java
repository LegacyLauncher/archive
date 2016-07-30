package ru.turikhay.util.git;

import java.util.Map;

public class MapTokenResolver implements ITokenResolver {
   private final Map tokenMap;

   public MapTokenResolver(Map tokenMap) {
      this.tokenMap = tokenMap;
   }

   public String resolveToken(String tokenName) {
      return (String)this.tokenMap.get(tokenName);
   }
}
