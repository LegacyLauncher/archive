package net.minecraft.launcher_.versions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.launcher_.OperatingSystem;

public class Rule {
   private Rule.Action action;
   private Rule.OSRestriction os;

   public Rule() {
      this.action = Rule.Action.ALLOW;
   }

   public Rule.Action getAppliedAction() {
      return this.os != null && !this.os.isCurrentOperatingSystem() ? null : this.action;
   }

   public String toString() {
      return "Rule{action=" + this.action + ", os=" + this.os + '}';
   }

   public static enum Action {
      ALLOW,
      DISALLOW;
   }

   public class OSRestriction {
      private OperatingSystem name;
      private String version;

      public boolean isCurrentOperatingSystem() {
         if (this.name != null && this.name != OperatingSystem.getCurrentPlatform()) {
            return false;
         } else {
            if (this.version != null) {
               try {
                  Pattern pattern = Pattern.compile(this.version);
                  Matcher matcher = pattern.matcher(System.getProperty("os.version"));
                  if (!matcher.matches()) {
                     return false;
                  }
               } catch (Throwable var3) {
               }
            }

            return true;
         }
      }

      public String toString() {
         return "OSRestriction{name=" + this.name + ", version='" + this.version + '\'' + '}';
      }
   }
}
