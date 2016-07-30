package ru.turikhay.tlauncher.ui.logger;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.Document;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedTextArea;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.pastebin.Paste;
import ru.turikhay.util.pastebin.PasteResult;
import ru.turikhay.util.stream.LinkedOutputStringStream;
import ru.turikhay.util.stream.PrintLogger;
import ru.turikhay.util.stream.StreamLogger;

public class Logger implements StreamLogger {
   private static List frames = Collections.synchronizedList(new ArrayList());
   public final LoggerFrame frame;
   private final Configuration global;
   private String name;
   private LinkedOutputStringStream stream;
   private PrintLogger logger;
   private Logger.CloseAction close;
   private boolean killed;
   MinecraftLauncher launcher;
   private FileExplorer explorer;

   public Logger(Configuration global, PrintLogger logger, String name, boolean show) {
      this.global = global;
      this.name = name;
      this.frame = new LoggerFrame(this);
      this.frame.setTitle(name);
      frames.add(new WeakReference(this.frame));
      this.update();
      this.frame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            Logger.this.onClose();
         }

         public void windowClosed(WindowEvent e) {
            U.log("Logger", Logger.this.name, "has been disposed.");
         }
      });
      this.frame.addComponentListener(new ExtendedComponentAdapter(this.frame) {
         public void componentShown(ComponentEvent e) {
            Logger.this.delayedSave();
         }

         public void componentHidden(ComponentEvent e) {
            Logger.this.delayedSave();
         }

         public void onComponentResized(ComponentEvent e) {
            Logger.this.delayedSave();
         }

         public void onComponentMoved(ComponentEvent e) {
            Logger.this.delayedSave();
         }
      });
      if (logger == null) {
         this.logger = null;
         this.stream = new LinkedOutputStringStream();
         this.stream.setLogger(this);
      } else {
         this.logger = logger;
         this.stream = logger.getStream();
      }

      if (show) {
         this.show();
      }

      this.stream.flush();
      if (logger != null) {
         logger.setMirror(this);
      }

   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
      this.frame.setTitle(name);
   }

   public MinecraftLauncher getLauncher() {
      return this.launcher;
   }

   public void setLauncher(MinecraftLauncher launcher) {
      this.launcher = launcher;
      this.frame.bottom.folder.setEnabled(true);
      if (launcher != null) {
         this.frame.bottom.kill.setEnabled(true);
         this.frame.bottom.openFolder = launcher.getGameDir();
      } else {
         this.frame.bottom.kill.setEnabled(false);
         if (Localizable.get("logger").equals(this.name)) {
            this.frame.bottom.openFolder = MinecraftUtil.getWorkingDirectory();
         }
      }

   }

   public void log(String s) {
      if (this.logger != null) {
         this.logger.rawlog(s);
      } else {
         this.stream.write(s.toCharArray());
      }

   }

   public void log(Object... o) {
      this.log(U.toLog(o));
   }

   public void rawlog(String s) {
      if (StringUtil.lastChar(s) == '\n') {
         this.frame.print(s);
      } else {
         this.frame.println(s);
      }

   }

   public void rawlog(char[] c) {
      this.rawlog(new String(c));
   }

   public CharSequence getOutput() {
      Document d = this.frame.textarea.getDocument();
      return d instanceof ExtendedTextArea.ContentDocument ? ((ExtendedTextArea.ContentDocument)d).accessContent() : null;
   }

   void update() {
      this.check();
      if (this.global != null) {
         String prefix = "gui.logger.";
         int width = this.global.getInteger(prefix + "width", 670);
         int height = this.global.getInteger(prefix + "height", 500);
         int x = this.global.getInteger(prefix + "x", 0);
         int y = this.global.getInteger(prefix + "y", 0);
         this.frame.setSize(width, height);
         this.frame.setLocation(x, y);
      }

   }

   void save() {
      this.check();
      if (this.global != null) {
         String prefix = "gui.logger.";
         int[] size = this.getSize();
         int[] position = this.getPosition();
         this.global.set(prefix + "width", size[0], false);
         this.global.set(prefix + "height", size[1], false);
         this.global.set(prefix + "x", position[0], false);
         this.global.set(prefix + "y", position[1], false);
      }

   }

   void delayedSave() {
      if (!this.killed) {
         this.save();
      }
   }

   private void check() {
      if (this.killed) {
         throw new IllegalStateException("Logger is already killed!");
      }
   }

   public void show() {
      this.show(true);
   }

   public void show(boolean toFront) {
      this.check();
      this.frame.setVisible(true);
      this.frame.scrollDown();
      if (toFront) {
         this.frame.toFront();
      }

   }

   public void hide() {
      this.check();
      this.frame.setVisible(false);
   }

   public void clear() {
      this.check();
      this.stream.flush();
      this.frame.clear();
   }

   public void kill() {
      this.check();
      this.save();
      this.frame.dispose();
      this.frame.clear();
      this.killed = true;
   }

   public void killIn(long millis) {
      this.check();
      this.save();
      this.frame.hideIn(millis);
   }

   public void sendPaste() {
      if (Alert.showLocQuestion("logger.pastebin.alert")) {
         AsyncThread.execute(new Runnable() {
            public void run() {
               Paste paste = new Paste();
               paste.addListener(Logger.this.frame);
               paste.setTitle(Logger.this.frame.getTitle());
               paste.setContent(Logger.this.frame.logger.getOutput());
               PasteResult result = paste.paste();
               if (result instanceof PasteResult.PasteUploaded) {
                  PasteResult.PasteUploaded error = (PasteResult.PasteUploaded)result;
                  if (Alert.showLocQuestion("logger.pastebin.sent", error.getURL())) {
                     OS.openLink(error.getURL());
                  }
               } else if (result instanceof PasteResult.PasteFailed) {
                  Throwable error1 = ((PasteResult.PasteFailed)result).getError();
                  if (error1 instanceof RuntimeException) {
                     Alert.showLocError("logger.pastebin.invalid", error1);
                  } else if (error1 instanceof IOException) {
                     Alert.showLocError("logger.pastebin.failed", error1);
                  }
               }

            }
         });
      }
   }

   public void saveAs() {
      if (this.explorer == null) {
         try {
            this.explorer = FileExplorer.newExplorer();
         } catch (Exception var16) {
            Alert.showError(Localizable.get("explorer.unavailable.title"), Localizable.get("explorer.unvailable") + (OS.WINDOWS.isCurrent() ? "\n" + Localizable.get("explorer.unavailable.win") : ""));
            return;
         }
      }

      this.explorer.setSelectedFile(new File(this.getName() + ".log"));
      int result = this.explorer.showSaveDialog(this.frame);
      if (result == 0) {
         File file = this.explorer.getSelectedFile();
         if (file != null) {
            String path = file.getAbsolutePath();
            if (!path.endsWith(".log")) {
               path = path + ".log";
            }

            file = new File(path);
            FileOutputStream output = null;

            try {
               FileUtil.createFile(file);
               IOUtils.copy((Reader)(new CharSequenceReader(this.getOutput())), (OutputStream)(output = new FileOutputStream(file)));
            } catch (Throwable var15) {
               Alert.showLocError("logger.save.error", var15);
            } finally {
               if (output != null) {
                  try {
                     output.close();
                  } catch (IOException var14) {
                     var14.printStackTrace();
                  }
               }

            }
         }
      }

   }

   public boolean isKilled() {
      this.check();
      return this.killed;
   }

   Point getPositionPoint() {
      this.check();
      return this.frame.getLocation();
   }

   int[] getPosition() {
      this.check();
      Point p = this.getPositionPoint();
      return new int[]{p.x, p.y};
   }

   Dimension getDimension() {
      this.check();
      return this.frame.getSize();
   }

   int[] getSize() {
      this.check();
      Dimension d = this.getDimension();
      return new int[]{d.width, d.height};
   }

   public void setCloseAction(Logger.CloseAction action) {
      this.close = action;
   }

   private void onClose() {
      if (this.close != null) {
         switch(this.close) {
         case EXIT:
            this.kill();
         case KILL:
            TLauncher.kill();
         }
      }

   }

   public static void updateLocale() {
      Iterator var0 = frames.iterator();

      while(var0.hasNext()) {
         WeakReference ref = (WeakReference)var0.next();
         LoggerFrame frame = (LoggerFrame)ref.get();
         if (frame != null) {
            frame.updateLocale();
         }
      }

   }

   public static void wipeAll() {
      Iterator var0 = frames.iterator();

      while(var0.hasNext()) {
         WeakReference ref = (WeakReference)var0.next();
         LoggerFrame frame = (LoggerFrame)ref.get();
         if (frame != null) {
            frame.clear();
         }
      }

   }

   public static enum CloseAction {
      KILL,
      EXIT;
   }
}
