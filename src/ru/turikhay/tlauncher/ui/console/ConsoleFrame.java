package ru.turikhay.tlauncher.ui.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.ExtensionFileFilter;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.EmptyAction;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.stream.StringStream;

public class ConsoleFrame extends JFrame implements LocalizableComponent {
   public static final int MIN_WIDTH = 670;
   public static final int MIN_HEIGHT = 500;
   public final Console console;
   public final JTextArea textarea;
   public final JScrollBar vScrollbar;
   public final ConsoleFrameBottom bottom;
   private int lastWindowWidth;
   private int scrollBarValue;
   private boolean scrollDown;
   private final Object busy = new Object();
   boolean hiding;

   ConsoleFrame(Console console) {
      this.console = console;
      this.textarea = new JTextArea();
      this.textarea.setLineWrap(true);
      this.textarea.setEditable(false);
      this.textarea.setAutoscrolls(true);
      this.textarea.setMargin(new Insets(0, 0, 0, 0));
      this.textarea.setFont(new Font("DialogInput", 0, (int)((double)(new LocalizableLabel()).getFont().getSize() * 1.2D)));
      this.textarea.setForeground(Color.white);
      this.textarea.setCaretColor(Color.white);
      this.textarea.setBackground(Color.black);
      this.textarea.setSelectionColor(Color.gray);
      ((DefaultCaret)this.textarea.getCaret()).setUpdatePolicy(2);
      this.textarea.addMouseListener(new ConsoleFrame.ConsoleTextPopup());
      ScrollPane scrollPane = new ScrollPane(this.textarea);
      scrollPane.setBorder((Border)null);
      scrollPane.setVBPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
      this.vScrollbar = scrollPane.getVerticalScrollBar();
      final BoundedRangeModel vsbModel = this.vScrollbar.getModel();
      this.vScrollbar.addAdjustmentListener(new AdjustmentListener() {
         public void adjustmentValueChanged(AdjustmentEvent e) {
            if (ConsoleFrame.this.getWidth() == ConsoleFrame.this.lastWindowWidth) {
               int nv = e.getValue();
               if (nv < ConsoleFrame.this.scrollBarValue) {
                  ConsoleFrame.this.scrollDown = false;
               } else if (nv == vsbModel.getMaximum() - vsbModel.getExtent()) {
                  ConsoleFrame.this.scrollDown = true;
               }

               ConsoleFrame.this.scrollBarValue = nv;
            }
         }
      });
      this.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            ConsoleFrame.this.lastWindowWidth = ConsoleFrame.this.getWidth();
         }
      });
      this.getContentPane().setLayout(new BorderLayout());
      this.getContentPane().add(scrollPane, "Center");
      this.getContentPane().add(this.bottom = new ConsoleFrameBottom(this), "South");
      SwingUtil.setFavicons(this);
   }

   public void println(String string) {
      this.print(string + '\n');
   }

   public void print(String string) {
      synchronized(this.busy) {
         Document document = this.textarea.getDocument();

         try {
            document.insertString(document.getLength(), string, (AttributeSet)null);
         } catch (Throwable var5) {
         }

         if (this.scrollDown) {
            this.scrollDown();
         }

      }
   }

   public void clear() {
      this.textarea.setText("");
   }

   public void scrollDown() {
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            ConsoleFrame.this.vScrollbar.setValue(ConsoleFrame.this.vScrollbar.getMaximum());
         }
      });
   }

   public void updateLocale() {
      Localizable.updateContainer(this);
   }

   void hideIn(long millis) {
      this.hiding = true;
      this.bottom.closeCancelButton.setVisible(true);
      this.bottom.closeCancelButton.setText("console.close.cancel", millis / 1000L);
      AsyncThread.execute(new Runnable(millis) {
         long remaining;

         {
            this.remaining = var2;
         }

         public void run() {
            ConsoleFrame.this.bottom.closeCancelButton.setText("console.close.cancel", this.remaining / 1000L);
            U.log(this.remaining);

            while(ConsoleFrame.this.hiding && this.remaining > 1999L) {
               this.remaining -= 1000L;
               ConsoleFrame.this.bottom.closeCancelButton.setText("console.close.cancel", this.remaining / 1000L);
               U.sleepFor(1000L);
            }

            if (ConsoleFrame.this.hiding) {
               ConsoleFrame.this.dispose();
            }

         }
      });
   }

   public class ConsoleTextPopup extends TextPopup {
      private final FileExplorer explorer = new FileExplorer();
      private final Action saveAllAction;
      private final Action clearAllAction;

      ConsoleTextPopup() {
         this.explorer.setFileFilter(new ExtensionFileFilter("log"));
         this.saveAllAction = new EmptyAction() {
            public void actionPerformed(ActionEvent e) {
               ConsoleTextPopup.this.onSavingCalled();
            }
         };
         this.clearAllAction = new EmptyAction() {
            public void actionPerformed(ActionEvent e) {
               ConsoleTextPopup.this.onClearCalled();
            }
         };
      }

      protected JPopupMenu getPopup(MouseEvent e, JTextComponent comp) {
         JPopupMenu menu = super.getPopup(e, comp);
         if (menu == null) {
            return null;
         } else {
            menu.addSeparator();
            menu.add(this.saveAllAction).setText(Localizable.get("console.save.popup"));
            menu.addSeparator();
            menu.add(this.clearAllAction).setText(Localizable.get("console.clear.popup"));
            return menu;
         }
      }

      protected void onSavingCalled() {
         this.explorer.setSelectedFile(new File(ConsoleFrame.this.console.getName() + ".log"));
         int result = this.explorer.showSaveDialog(ConsoleFrame.this.console.frame);
         if (result == 0) {
            File file = this.explorer.getSelectedFile();
            if (file == null) {
               U.log("Returned NULL. Damn it!");
            } else {
               String path = file.getAbsolutePath();
               if (!path.endsWith(".log")) {
                  path = path + ".log";
               }

               file = new File(path);
               BufferedOutputStream output = null;

               try {
                  FileUtil.createFile(file);
                  StringStream input = ConsoleFrame.this.console.getStream();
                  output = new BufferedOutputStream(new FileOutputStream(file));
                  boolean addR = OS.WINDOWS.isCurrent();
                  int caret = -1;

                  while(true) {
                     ++caret;
                     if (caret >= input.getLength()) {
                        output.close();
                        break;
                     }

                     char current = input.getCharAt(caret);
                     if (current == '\n' && addR) {
                        output.write(13);
                     }

                     output.write(current);
                  }
               } catch (Throwable var17) {
                  Alert.showLocError("console.save.error", var17);
               } finally {
                  if (output != null) {
                     try {
                        output.close();
                     } catch (IOException var16) {
                        var16.printStackTrace();
                     }
                  }

               }

            }
         }
      }

      protected void onClearCalled() {
         ConsoleFrame.this.console.clear();
      }
   }
}
