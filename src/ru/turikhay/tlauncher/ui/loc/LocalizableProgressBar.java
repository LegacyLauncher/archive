package ru.turikhay.tlauncher.ui.loc;

import java.awt.Component;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;

public class LocalizableProgressBar extends ProgressBar implements LocalizableComponent {
   private String westPath;
   private String centerPath;
   private String eastPath;
   private String[] westVars;
   private String[] centerVars;
   private String[] eastVars;

   protected LocalizableProgressBar(Component parentComp) {
      super(parentComp);
   }

   public void setWestString(String path, boolean update, Object... vars) {
      this.westPath = path;
      this.westVars = Localizable.checkVariables(vars);
      super.setWestString(Localizable.get(this.westPath, this.westVars), update);
   }

   public void setWestString(String path, boolean update) {
      this.setWestString(path, update, Localizable.EMPTY_VARS);
   }

   public void setCenterString(String path, boolean update, Object... vars) {
      this.centerPath = path;
      this.centerVars = Localizable.checkVariables(vars);
      super.setCenterString(Localizable.get(this.centerPath, this.centerVars), update);
   }

   public void setCenterString(String path, boolean update) {
      this.setCenterString(path, update, Localizable.EMPTY_VARS);
   }

   public void setEastString(String path, boolean update, Object... vars) {
      this.eastPath = path;
      this.eastVars = Localizable.checkVariables(vars);
      super.setEastString(Localizable.get(this.eastPath, this.eastVars), update);
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
