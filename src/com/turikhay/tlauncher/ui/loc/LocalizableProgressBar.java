package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.progress.ProgressBar;
import java.awt.Component;

public class LocalizableProgressBar extends ProgressBar implements LocalizableComponent {
   private static final long serialVersionUID = 7393243528402135898L;
   private String westPath;
   private String centerPath;
   private String eastPath;
   private String[] westVars;
   private String[] centerVars;
   private String[] eastVars;

   public LocalizableProgressBar(Component parentComp) {
      super(parentComp);
   }

   public LocalizableProgressBar() {
      this((Component)null);
   }

   public void setWestString(String path, boolean update, Object... vars) {
      this.westPath = path;
      this.westVars = Localizable.checkVariables(vars);
      String value = Localizable.get(path);

      for(int i = 0; i < this.westVars.length; ++i) {
         value = value.replace("%" + i, this.westVars[i]);
      }

      super.setWestString(value, update);
   }

   public void setWestString(String path, boolean update) {
      this.setWestString(path, update, Localizable.EMPTY_VARS);
   }

   public void setWestString(String path, Object... vars) {
      this.setWestString(path, true, vars);
   }

   public void setCenterString(String path, boolean update, Object... vars) {
      this.centerPath = path;
      this.centerVars = Localizable.checkVariables(vars);
      String value = Localizable.get(path);

      for(int i = 0; i < this.centerVars.length; ++i) {
         value = value.replace("%" + i, this.centerVars[i]);
      }

      super.setCenterString(value, update);
   }

   public void setCenterString(String path, boolean update) {
      this.setCenterString(path, update, Localizable.EMPTY_VARS);
   }

   public void setCenterString(String path, Object... vars) {
      this.setCenterString(path, true, vars);
   }

   public void setEastString(String path, boolean update, Object... vars) {
      this.eastPath = path;
      this.eastVars = Localizable.checkVariables(vars);
      String value = Localizable.get(path);

      for(int i = 0; i < this.eastVars.length; ++i) {
         value = value.replace("%" + i, this.eastVars[i]);
      }

      super.setEastString(value, update);
   }

   public void setEastString(String path, boolean update) {
      this.setEastString(path, update, Localizable.EMPTY_VARS);
   }

   public void setEastString(String path, Object... vars) {
      this.setEastString(path, true, vars);
   }

   public void setStrings(String west, String center, String east, boolean acceptNull, boolean repaint, boolean saveVars) {
      if (acceptNull || west != null) {
         this.setWestString(west, false, (Object[])(saveVars ? this.westVars : Localizable.EMPTY_VARS));
      }

      if (acceptNull || center != null) {
         this.setCenterString(center, false, (Object[])(saveVars ? this.centerVars : Localizable.EMPTY_VARS));
      }

      if (acceptNull || east != null) {
         this.setEastString(east, false, (Object[])(saveVars ? this.eastVars : Localizable.EMPTY_VARS));
      }

      this.repaint();
   }

   public void setStrings(String west, String center, String east, boolean acceptNull, boolean repaint) {
      this.setStrings(west, center, east, acceptNull, repaint, false);
   }

   public void updateLocale() {
      this.setStrings(this.westPath, this.centerPath, this.eastPath, true, true);
   }
}
