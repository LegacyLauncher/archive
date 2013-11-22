package org.apache.commons.lang3.text;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class StrSubstitutor {
   public static final char DEFAULT_ESCAPE = '$';
   public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher("${");
   public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher("}");
   private char escapeChar;
   private StrMatcher prefixMatcher;
   private StrMatcher suffixMatcher;
   private StrLookup variableResolver;
   private boolean enableSubstitutionInVariables;

   public static String replace(Object source, Map valueMap) {
      return (new StrSubstitutor(valueMap)).replace(source);
   }

   public static String replace(Object source, Map valueMap, String prefix, String suffix) {
      return (new StrSubstitutor(valueMap, prefix, suffix)).replace(source);
   }

   public static String replace(Object source, Properties valueProperties) {
      if (valueProperties == null) {
         return source.toString();
      } else {
         Map valueMap = new HashMap();
         Enumeration propNames = valueProperties.propertyNames();

         while(propNames.hasMoreElements()) {
            String propName = (String)propNames.nextElement();
            String propValue = valueProperties.getProperty(propName);
            valueMap.put(propName, propValue);
         }

         return replace(source, (Map)valueMap);
      }
   }

   public static String replaceSystemProperties(Object source) {
      return (new StrSubstitutor(StrLookup.systemPropertiesLookup())).replace(source);
   }

   public StrSubstitutor() {
      this((StrLookup)null, (StrMatcher)DEFAULT_PREFIX, (StrMatcher)DEFAULT_SUFFIX, '$');
   }

   public StrSubstitutor(Map valueMap) {
      this(StrLookup.mapLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
   }

   public StrSubstitutor(Map valueMap, String prefix, String suffix) {
      this(StrLookup.mapLookup(valueMap), prefix, suffix, '$');
   }

   public StrSubstitutor(Map valueMap, String prefix, String suffix, char escape) {
      this(StrLookup.mapLookup(valueMap), prefix, suffix, escape);
   }

   public StrSubstitutor(StrLookup variableResolver) {
      this(variableResolver, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
   }

   public StrSubstitutor(StrLookup variableResolver, String prefix, String suffix, char escape) {
      this.setVariableResolver(variableResolver);
      this.setVariablePrefix(prefix);
      this.setVariableSuffix(suffix);
      this.setEscapeChar(escape);
   }

   public StrSubstitutor(StrLookup variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape) {
      this.setVariableResolver(variableResolver);
      this.setVariablePrefixMatcher(prefixMatcher);
      this.setVariableSuffixMatcher(suffixMatcher);
      this.setEscapeChar(escape);
   }

   public String replace(String source) {
      if (source == null) {
         return null;
      } else {
         StrBuilder buf = (new StrBuilder()).append(source);
         this.substitute(buf, 0, buf.length());
         return buf.toString();
      }
   }

   public String replace(String source, int offset, int length) {
      if (source == null) {
         return null;
      } else {
         StrBuilder buf = (new StrBuilder(length)).append(source, offset, length);
         return !this.substitute(buf, 0, length) ? source.substring(offset, offset + length) : buf.toString();
      }
   }

   public String replace(char[] source) {
      if (source == null) {
         return null;
      } else {
         StrBuilder buf = (new StrBuilder(source.length)).append((Object)source);
         this.substitute(buf, 0, source.length);
         return buf.toString();
      }
   }

   public String replace(StringBuffer source) {
      if (source == null) {
         return null;
      } else {
         StrBuilder buf = (new StrBuilder(source.length())).append((CharSequence)source);
         this.substitute(buf, 0, buf.length());
         return buf.toString();
      }
   }

   public String replace(StringBuffer source, int offset, int length) {
      if (source == null) {
         return null;
      } else {
         StrBuilder buf = (new StrBuilder(length)).append((CharSequence)source, offset, length);
         this.substitute(buf, 0, length);
         return buf.toString();
      }
   }

   public String replace(StrBuilder source) {
      if (source == null) {
         return null;
      } else {
         StrBuilder buf = (new StrBuilder(source.length())).append((CharSequence)source);
         this.substitute(buf, 0, buf.length());
         return buf.toString();
      }
   }

   public String replace(StrBuilder source, int offset, int length) {
      if (source == null) {
         return null;
      } else {
         StrBuilder buf = (new StrBuilder(length)).append((CharSequence)source, offset, length);
         this.substitute(buf, 0, length);
         return buf.toString();
      }
   }

   public String replace(Object source) {
      if (source == null) {
         return null;
      } else {
         StrBuilder buf = (new StrBuilder()).append(source);
         this.substitute(buf, 0, buf.length());
         return buf.toString();
      }
   }

   public boolean replaceIn(StringBuffer source) {
      return source == null ? false : this.replaceIn((StringBuffer)source, 0, source.length());
   }

   public boolean replaceIn(StringBuffer source, int offset, int length) {
      if (source == null) {
         return false;
      } else {
         StrBuilder buf = (new StrBuilder(length)).append((CharSequence)source, offset, length);
         if (!this.substitute(buf, 0, length)) {
            return false;
         } else {
            source.replace(offset, offset + length, buf.toString());
            return true;
         }
      }
   }

   public boolean replaceIn(StrBuilder source) {
      return source == null ? false : this.substitute(source, 0, source.length());
   }

   public boolean replaceIn(StrBuilder source, int offset, int length) {
      return source == null ? false : this.substitute(source, offset, length);
   }

   protected boolean substitute(StrBuilder buf, int offset, int length) {
      return this.substitute(buf, offset, length, (List)null) > 0;
   }

   private int substitute(StrBuilder buf, int offset, int length, List priorVariables) {
      StrMatcher prefixMatcher = this.getVariablePrefixMatcher();
      StrMatcher suffixMatcher = this.getVariableSuffixMatcher();
      char escape = this.getEscapeChar();
      boolean top = priorVariables == null;
      boolean altered = false;
      int lengthChange = 0;
      char[] chars = buf.buffer;
      int bufEnd = offset + length;
      int pos = offset;

      while(true) {
         label69:
         while(pos < bufEnd) {
            int startMatchLen = prefixMatcher.isMatch(chars, pos, offset, bufEnd);
            if (startMatchLen == 0) {
               ++pos;
            } else if (pos > offset && chars[pos - 1] == escape) {
               buf.deleteCharAt(pos - 1);
               chars = buf.buffer;
               --lengthChange;
               altered = true;
               --bufEnd;
            } else {
               int startPos = pos;
               pos += startMatchLen;
               int endMatchLen = false;
               int nestedVarCount = 0;

               while(true) {
                  while(true) {
                     if (pos >= bufEnd) {
                        continue label69;
                     }

                     int endMatchLen;
                     if (this.isEnableSubstitutionInVariables() && (endMatchLen = prefixMatcher.isMatch(chars, pos, offset, bufEnd)) != 0) {
                        ++nestedVarCount;
                        pos += endMatchLen;
                     } else {
                        endMatchLen = suffixMatcher.isMatch(chars, pos, offset, bufEnd);
                        if (endMatchLen == 0) {
                           ++pos;
                        } else {
                           if (nestedVarCount == 0) {
                              String varName = new String(chars, startPos + startMatchLen, pos - startPos - startMatchLen);
                              if (this.isEnableSubstitutionInVariables()) {
                                 StrBuilder bufName = new StrBuilder(varName);
                                 this.substitute(bufName, 0, bufName.length());
                                 varName = bufName.toString();
                              }

                              pos += endMatchLen;
                              if (priorVariables == null) {
                                 priorVariables = new ArrayList();
                                 ((List)priorVariables).add(new String(chars, offset, length));
                              }

                              this.checkCyclicSubstitution(varName, (List)priorVariables);
                              ((List)priorVariables).add(varName);
                              String varValue = this.resolveVariable(varName, buf, startPos, pos);
                              if (varValue != null) {
                                 int varLen = varValue.length();
                                 buf.replace(startPos, pos, varValue);
                                 altered = true;
                                 int change = this.substitute(buf, startPos, varLen, (List)priorVariables);
                                 change = change + varLen - (pos - startPos);
                                 pos += change;
                                 bufEnd += change;
                                 lengthChange += change;
                                 chars = buf.buffer;
                              }

                              ((List)priorVariables).remove(((List)priorVariables).size() - 1);
                              continue label69;
                           }

                           --nestedVarCount;
                           pos += endMatchLen;
                        }
                     }
                  }
               }
            }
         }

         if (top) {
            return altered ? 1 : 0;
         }

         return lengthChange;
      }
   }

   private void checkCyclicSubstitution(String varName, List priorVariables) {
      if (priorVariables.contains(varName)) {
         StrBuilder buf = new StrBuilder(256);
         buf.append("Infinite loop in property interpolation of ");
         buf.append((String)priorVariables.remove(0));
         buf.append(": ");
         buf.appendWithSeparators(priorVariables, "->");
         throw new IllegalStateException(buf.toString());
      }
   }

   protected String resolveVariable(String variableName, StrBuilder buf, int startPos, int endPos) {
      StrLookup resolver = this.getVariableResolver();
      return resolver == null ? null : resolver.lookup(variableName);
   }

   public char getEscapeChar() {
      return this.escapeChar;
   }

   public void setEscapeChar(char escapeCharacter) {
      this.escapeChar = escapeCharacter;
   }

   public StrMatcher getVariablePrefixMatcher() {
      return this.prefixMatcher;
   }

   public StrSubstitutor setVariablePrefixMatcher(StrMatcher prefixMatcher) {
      if (prefixMatcher == null) {
         throw new IllegalArgumentException("Variable prefix matcher must not be null!");
      } else {
         this.prefixMatcher = prefixMatcher;
         return this;
      }
   }

   public StrSubstitutor setVariablePrefix(char prefix) {
      return this.setVariablePrefixMatcher(StrMatcher.charMatcher(prefix));
   }

   public StrSubstitutor setVariablePrefix(String prefix) {
      if (prefix == null) {
         throw new IllegalArgumentException("Variable prefix must not be null!");
      } else {
         return this.setVariablePrefixMatcher(StrMatcher.stringMatcher(prefix));
      }
   }

   public StrMatcher getVariableSuffixMatcher() {
      return this.suffixMatcher;
   }

   public StrSubstitutor setVariableSuffixMatcher(StrMatcher suffixMatcher) {
      if (suffixMatcher == null) {
         throw new IllegalArgumentException("Variable suffix matcher must not be null!");
      } else {
         this.suffixMatcher = suffixMatcher;
         return this;
      }
   }

   public StrSubstitutor setVariableSuffix(char suffix) {
      return this.setVariableSuffixMatcher(StrMatcher.charMatcher(suffix));
   }

   public StrSubstitutor setVariableSuffix(String suffix) {
      if (suffix == null) {
         throw new IllegalArgumentException("Variable suffix must not be null!");
      } else {
         return this.setVariableSuffixMatcher(StrMatcher.stringMatcher(suffix));
      }
   }

   public StrLookup getVariableResolver() {
      return this.variableResolver;
   }

   public void setVariableResolver(StrLookup variableResolver) {
      this.variableResolver = variableResolver;
   }

   public boolean isEnableSubstitutionInVariables() {
      return this.enableSubstitutionInVariables;
   }

   public void setEnableSubstitutionInVariables(boolean enableSubstitutionInVariables) {
      this.enableSubstitutionInVariables = enableSubstitutionInVariables;
   }
}
