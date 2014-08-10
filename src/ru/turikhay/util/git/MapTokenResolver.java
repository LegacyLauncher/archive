package ru.turikhay.util.git;

import java.util.HashMap;
import java.util.Map;

public class MapTokenResolver implements ITokenResolver {
   protected Map tokenMap = new HashMap();

   public MapTokenResolver(Map tokenMap) {
      this.tokenMap = tokenMap;
   }

   public String resolveToken(String tokenName) {
      return (String)this.tokenMap.get(tokenName);
   }
}
