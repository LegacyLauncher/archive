package ru.turikhay.tlauncher.minecraft.crash;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.DXDiagScanner;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class CrashEntry extends IEntry {
   private boolean localizable = true;
   private boolean fake;
   private boolean permitHelp = true;
   private int exitCode;
   private List osList = new ArrayList(Arrays.asList(OS.values()));
   private List _osList;
   private boolean archIssue;
   private Pattern graphicsCardPattern;
   private Pattern versionPattern;
   private Pattern jrePattern;
   private String imagePath;
   private String title;
   private Object[] titleVars;
   private String body;
   private Object[] bodyVars;
   private final List buttons;
   private final List _buttons;

   public CrashEntry(CrashManager manager, String name) {
      super(manager, name);
      this._osList = Collections.unmodifiableList(this.osList);
      this.buttons = new ArrayList();
      this._buttons = Collections.unmodifiableList(this.buttons);
      this.setPath((String)null);
   }

   protected final void setLocalizable(boolean localizable) {
      this.localizable = localizable;
   }

   public final boolean isFake() {
      return this.fake;
   }

   protected final void setFake(boolean fake) {
      this.fake = fake;
   }

   public final boolean isPermitHelp() {
      return this.permitHelp;
   }

   public void setPermitHelp(boolean permitHelp) {
      this.permitHelp = permitHelp;
   }

   public final int getExitCode() {
      return this.exitCode;
   }

   protected final void setExitCode(int exitCode) {
      this.exitCode = exitCode;
   }

   public final boolean isCompatibleWith(OS os) {
      return this.osList.contains(os);
   }

   protected void setOS(OS... os) {
      this.osList.clear();
      Collections.addAll(this.osList, os);
   }

   public final boolean isArchIssue() {
      return this.archIssue;
   }

   protected final void setArchIssue(boolean archIssue) {
      this.archIssue = archIssue;
   }

   public final Pattern getGraphicsCardPattern() {
      return this.graphicsCardPattern;
   }

   protected final void setGraphicsCardPattern(Pattern graphicsCardPattern) {
      this.graphicsCardPattern = graphicsCardPattern;
   }

   public final Pattern getVersionPattern() {
      return this.versionPattern;
   }

   protected final void setVersionPattern(Pattern versionPattern) {
      this.versionPattern = versionPattern;
   }

   public final Pattern getJrePattern() {
      return this.jrePattern;
   }

   protected final void setJrePattern(Pattern jrePattern) {
      this.jrePattern = jrePattern;
   }

   public final String getImage() {
      return this.imagePath;
   }

   protected final void setImage(String imagePath) {
      this.imagePath = imagePath;
   }

   public final String getTitle() {
      return this.title;
   }

   protected final void setTitle(String title, Object... vars) {
      this.title = title;
      this.titleVars = vars;
   }

   public final Object[] getTitleVars() {
      return this.titleVars;
   }

   public final String getBody() {
      return this.body;
   }

   protected final void setBody(String body, Object... vars) {
      this.body = body;
      this.bodyVars = vars;
   }

   public final Object[] getBodyVars() {
      return this.bodyVars;
   }

   protected final void setPath(String path, Object... vars) {
      String prefix = this.getLocPath(path);
      this.setTitle(prefix + ".title", vars);
      this.setBody(prefix + ".body", vars);
   }

   public final List getButtons() {
      return this._buttons;
   }

   protected final void addButton(Button button) {
      this.buttons.add(U.requireNotNull(button));
   }

   protected final void clearButtons() {
      this.buttons.clear();
   }

   protected Button newButton(String text, Action action, Object... vars) {
      Button button = new Button(text);
      button.setLocalizable(true, false);
      button.setText(text, vars);
      button.getActions().add(action);
      this.addButton(button);
      return button;
   }

   protected boolean checkCapability() throws Exception {
      if (this.getVersionPattern() != null && !this.getVersionPattern().matcher(this.getManager().getVersion()).matches()) {
         this.log(new Object[]{"is not capable because of Minecraft version"});
         return false;
      } else if (this.getExitCode() != 0 && this.getExitCode() != this.getManager().getExitCode()) {
         this.log(new Object[]{"is not capable because of exit code"});
         return false;
      } else if (!this.isCompatibleWith(OS.CURRENT)) {
         this.log(new Object[]{"is not capable because of OS"});
         return false;
      } else if (this.getJrePattern() != null && !this.getJrePattern().matcher(System.getProperty("java.version")).matches()) {
         this.log(new Object[]{"is not capable because of Java version"});
         return false;
      } else if (this.getGraphicsCardPattern() != null) {
         this.log(new Object[]{"graphics card pattern", this.getGraphicsCardPattern()});
         if (!DXDiagScanner.isScannable()) {
            this.log(new Object[]{"is not capable because it requires DXDiag scanner"});
            return false;
         } else {
            DXDiagScanner.DXDiagScannerResult result;
            try {
               result = DXDiagScanner.getInstance().getResult();
            } catch (Exception var5) {
               this.log(new Object[]{"is not capable because DxDiag result is unavailable"});
               return false;
            }

            List deviceList = result.getDisplayDevices();
            if (deviceList != null && !deviceList.isEmpty()) {
               Iterator var3 = deviceList.iterator();

               DXDiagScanner.DXDiagScannerResult.DXDiagDisplayDevice device;
               do {
                  if (!var3.hasNext()) {
                     return false;
                  }

                  device = (DXDiagScanner.DXDiagScannerResult.DXDiagDisplayDevice)var3.next();
               } while(!this.getGraphicsCardPattern().matcher(device.getCardName()).matches());

               this.log(new Object[]{"is capable, found device:", device});
               return true;
            } else {
               this.log(new Object[]{"is not capable because display devices list is unavailable:", deviceList});
               return false;
            }
         }
      } else if (!this.isArchIssue()) {
         return true;
      } else {
         if (OS.Arch.x32.isCurrent() && DXDiagScanner.isScannable()) {
            boolean is64Bit = false;

            try {
               is64Bit = DXDiagScanner.getInstance().getResult().is64Bit();
            } catch (Exception var6) {
            }

            boolean result = OS.Arch.x32.isCurrent() && is64Bit;
            if (!result) {
               this.log(new Object[]{"is not capable because OS and Java arch are the same"});
            }
         }

         return false;
      }
   }

   String getLocPath(String path) {
      return path == null ? "crash." + this.getName() : "crash." + this.getName() + "." + path;
   }

   public ToStringBuilder buildToString() {
      return super.buildToString().append("exitCode", this.exitCode).append("fake", this.fake).append("permitHelp", this.permitHelp).append("os", this.osList).append("archIssue", this.archIssue).append("version", this.versionPattern).append("jre", this.jrePattern).append("graphics", this.graphicsCardPattern).append("title", this.getTitle()).append("body", this.getBody()).append("buttons", this.getButtons());
   }
}
