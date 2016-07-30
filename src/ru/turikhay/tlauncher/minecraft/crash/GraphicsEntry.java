package ru.turikhay.tlauncher.minecraft.crash;

import java.util.List;
import java.util.regex.Pattern;
import ru.turikhay.util.DXDiagScanner;
import ru.turikhay.util.OS;

public class GraphicsEntry extends PatternContainerEntry {
   private final Pattern intelBugJrePattern;
   private final PatternEntry general;
   private final PatternEntry amd;
   private final PatternEntry intel;

   public GraphicsEntry(CrashManager manager) {
      super(manager, "graphics");
      this.setAnyPatternMakesCapable(true);
      this.intelBugJrePattern = manager.getVar("intel-bug-jre-pattern") == null ? null : Pattern.compile("1\\.(?:[8-9]|[1-9][0-9]+)\\.[0-9](?:-.+|_(?!60)([1-9]?)(?:(1)[0-9]|[6-9])[0-9])");
      this.general = this.addPattern("general", Pattern.compile("[\\s]*org\\.lwjgl\\.LWJGLException\\: Pixel format not accelerated"));
      this.amd = this.addPattern("amd", Pattern.compile(manager.getVar("amd-pattern") == null ? "^#[ ]+C[ ]+\\[atio(?:gl|[0-9a-z]{2,})xx\\.dll\\+0x[0-9a-z]+\\]$" : manager.getVar("amd-pattern")));
      this.intel = this.addPattern("intel", Pattern.compile(manager.getVar("intel-pattern") == null ? "^#[ ]+C[ ]+\\[ig[0-9]+icd(?:32|64)\\.dll\\+0x[0-9a-z]+\\]$" : manager.getVar("intel-pattern")));
   }

   protected boolean checkCapability(List capablePatterns) {
      if (!OS.WINDOWS.isCurrent()) {
         this.setPath("general-linux", new Object[0]);
         return true;
      } else {
         this.setPath("general", new Object[0]);
         if (capablePatterns.contains(this.amd)) {
            return this.setToUpdateDrivers("AMD");
         } else if (capablePatterns.contains(this.intel)) {
            return this.setToUpdateDrivers("Intel");
         } else if (!DXDiagScanner.isScannable()) {
            return true;
         } else {
            DXDiagScanner.DXDiagScannerResult result;
            try {
               result = DXDiagScanner.getInstance().getResult();
            } catch (Exception var6) {
               this.log(new Object[]{"could not get dxdiag result", var6});
               return true;
            }

            label113: {
               if (OS.VERSION.contains("10.")) {
                  DXDiagScanner.DXDiagScannerResult.DXDiagDisplayDevice intelGraphics = result.getDisplayDevice("intel");
                  if (intelGraphics != null && intelGraphics.getCardName().matches(".+ [1-2][\\d]+$")) {
                     this.log(new Object[]{"DXDiag found 1st or 2nd generation of Intel chipset"});
                     this.log(new Object[]{"External pattern:", this.intelBugJrePattern != null});
                     if (this.intelBugJrePattern == null) {
                        if (OS.JAVA_VERSION.getDouble() >= 1.8D && OS.JAVA_VERSION.getUpdate() > 60) {
                           break label113;
                        }
                     } else if (this.intelBugJrePattern.matcher(System.getProperty("java.version")).matches()) {
                        break label113;
                     }
                  }
               }

               boolean haveIntel = result.getDisplayDevice("intel") != null;
               boolean haveNvidia = result.getDisplayDevice("nvidia") != null;
               boolean haveAmd = result.getDisplayDevice("amd") != null || result.getDisplayDevice("ati") != null;
               if (haveIntel) {
                  if (haveNvidia) {
                     this.setToUpdateDrivers("Intel", "NVIDIA");
                     this.newButton("intel-nvidia-select", new GraphicsEntry.VarUrlAction("intel-nvidia-select-url", "http://tlaun.ch/wiki/guide:select-intel-nvidia"), new Object[0]);
                     return true;
                  }

                  if (haveAmd) {
                     return this.setToUpdateDrivers("Intel", "AMD");
                  }

                  return this.setToUpdateDrivers("Intel");
               }

               if (haveAmd && haveNvidia) {
                  return this.setToUpdateDrivers("AMD", "NVIDIA");
               }

               if (haveNvidia) {
                  return this.setToUpdateDrivers("NVIDIA");
               }

               if (haveAmd) {
                  return this.setToUpdateDrivers("AMD");
               }

               return true;
            }

            this.log(new Object[]{"We're currently running Java version on Windows 10 that have known incompatibility bug with first- and -second generation Intel HD graphics chipsets"});
            this.clearButtons();
            this.setPath("intel.downgrade-to-jre8u60", new Object[0]);
            this.newButton("intel.buttons.downgrade-to-jre8u60", new GraphicsEntry.VarUrlAction("intel-bug-jre-link", "http://tlaun.ch/wiki/trbl:intel-8u60"), new Object[0]);
            return true;
         }
      }
   }

   private boolean setToUpdateDrivers(String... manufacturers) {
      this.clearButtons();
      this.log(new Object[]{"offering to update drivers for:", manufacturers});
      StringBuilder nameBuilder = new StringBuilder();
      String[] var3 = manufacturers;
      int var4 = manufacturers.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String manufacturerName = var3[var5];
         nameBuilder.append(", ").append(manufacturerName);
         String manufacturer = manufacturerName.toLowerCase();
         this.newButton("driver-update", new GraphicsEntry.VarUrlAction(manufacturer + "-driver-update", "http://tlaun.ch/wiki/update:driver:" + manufacturer), new Object[]{manufacturerName});
      }

      this.setPath("update-driver", new Object[]{nameBuilder.substring(", ".length())});
      if (manufacturers.length == 1) {
         this.setImage("manufacturer-" + manufacturers[0] + ".png");
      }

      return true;
   }

   private class VarUrlAction implements Action {
      private String url;

      VarUrlAction(String varName, String fallbackUrl) {
         String url = GraphicsEntry.this.getManager().getVar(varName);
         if (url == null) {
            url = fallbackUrl;
         }

         this.url = url;
      }

      public void execute() throws Exception {
         OS.openLink(this.url);
      }
   }
}
