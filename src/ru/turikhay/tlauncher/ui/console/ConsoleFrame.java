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
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.EmptyAction;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.pastebin.Paste;
import ru.turikhay.util.pastebin.PasteListener;

public class ConsoleFrame extends JFrame implements LocalizableComponent, PasteListener {
   public final Console console;
   public final JTextArea textarea;
   public final JScrollBar vScrollbar;
   public final ConsoleFrameBottom bottom;
   public final ConsoleFrame.ConsoleTextPopup popup;
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
      this.popup = new ConsoleFrame.ConsoleTextPopup();
      this.textarea.addMouseListener(this.popup);
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
      Object var2 = this.busy;
      synchronized(this.busy) {
         Document document = this.textarea.getDocument();

         try {
            document.insertString(document.getLength(), string, (AttributeSet)null);
         } catch (Throwable var7) {
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

   public void pasteUploading(Paste paste) {
      this.bottom.pastebin.setEnabled(false);
      this.popup.pastebinAction.setEnabled(false);
   }

   public void pasteDone(Paste paste) {
      this.bottom.pastebin.setEnabled(true);
      this.popup.pastebinAction.setEnabled(true);
   }

   void hideIn(final long millis) {
      this.hiding = true;
      this.bottom.closeCancelButton.setVisible(true);
      this.bottom.closeCancelButton.setText("console.close.cancel", millis / 1000L);
      AsyncThread.execute(new Runnable() {
         long remaining = millis;

         public void run() {
            ConsoleFrame.this.bottom.closeCancelButton.setText("console.close.cancel", this.remaining / 1000L);

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
      private final Action saveAllAction = new EmptyAction() {
         public void actionPerformed(ActionEvent e) {
            ConsoleFrame.this.console.saveAs();
         }
      };
      private final Action pastebinAction = new EmptyAction() {
         public void actionPerformed(ActionEvent e) {
            ConsoleFrame.this.console.sendPaste();
         }
      };
      private final Action clearAllAction = new EmptyAction() {
         public void actionPerformed(ActionEvent e) {
            ConsoleTextPopup.this.onClearCalled();
         }
      };

      protected JPopupMenu getPopup(MouseEvent e, JTextComponent comp) {
         JPopupMenu menu = super.getPopup(e, comp);
         if (menu == null) {
            return null;
         } else {
            menu.addSeparator();
            menu.add(this.saveAllAction).setText(Localizable.get("console.save.popup"));
            menu.add(this.pastebinAction).setText(Localizable.get("console.pastebin"));
            menu.addSeparator();
            menu.add(this.clearAllAction).setText(Localizable.get("console.clear.popup"));
            return menu;
         }
      }

      protected void onClearCalled() {
         ConsoleFrame.this.console.clear();
      }
   }
}
