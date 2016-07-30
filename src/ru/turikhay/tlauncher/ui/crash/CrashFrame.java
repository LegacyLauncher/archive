package ru.turikhay.tlauncher.ui.crash;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.minecraft.crash.Button;
import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashEntry;
import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.support.PreSupportFrame;
import ru.turikhay.tlauncher.ui.support.SupportFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public final class CrashFrame extends VActionFrame {
   private final ImageIcon crashIcon = Images.getIcon("graphic-design.png");
   private final PreSupportFrame supportFrame = new PreSupportFrame() {
      protected void onSupportFrameSelected(SupportFrame frame) {
         CrashFrame.this.setVisible(false);
         super.onSupportFrameSelected(frame);
      }
   };
   private Crash crash;
   private final LocalizableButton openLogs = new LocalizableButton("crash.buttons.logs");
   private final LocalizableButton askHelp;
   private final LocalizableButton exitButton;
   private String logPrefix;

   CrashFrame(CrashProcessingFrame frame) {
      this.openLogs.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            if (CrashFrame.this.crash != null && CrashFrame.this.crash.getManager().getLauncher() != null && CrashFrame.this.crash.getManager().getLauncher().getLogger() != null && !CrashFrame.this.crash.getManager().getLauncher().getLogger().isKilled()) {
               CrashFrame.this.crash.getManager().getLauncher().getLogger().show(true);
            } else {
               TLauncher.getLogger().show(true);
            }

         }
      });
      this.askHelp = new LocalizableButton("crash.buttons.support");
      this.askHelp.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            CrashFrame.this.supportFrame.showAtCenter();
         }
      });
      this.exitButton = new LocalizableButton("crash.buttons.exit");
      this.askHelp.setPreferredSize(SwingUtil.magnify(new Dimension(150, 25)));
      this.exitButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 25)));
      this.exitButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            CrashFrame.this.setVisible(false);
         }
      });
      this.logPrefix = "[]";
      this.getFooter().setLayout(new GridBagLayout());
   }

   public void setCrash(Crash crash) {
      this.crash = (Crash)U.requireNotNull(crash);
      this.logPrefix = "[CrashFrame]" + (crash.getEntry() == null ? "[unknown]" : "[" + crash.getEntry().getName() + "]");
      if (crash.getEntry() == null) {
         this.initOnUnknown();
      } else {
         if (crash.getEntry().isFake()) {
            this.log("Crash is fake, ignoring");
            return;
         }

         this.initOnCrash(crash.getEntry());
      }

      this.pack();
      this.showAtCenter();
   }

   private void initOnUnknown() {
      this.log("Unknown crash proceeded");
      this.setTitlePath("crash.unknown.title", new Object[0]);
      this.getHead().setText("crash.unknown.title");
      this.getHead().setIcon(Images.getIcon("graphic-design.png", 32));
      this.getBodyText().setText("crash.unknown.body");
      this.setButtons(true, this.openLogs);
   }

   private void initOnCrash(CrashEntry entry) {
      this.log("Crash entry proceeded:", entry);
      this.setTitlePath(entry.getTitle(), entry.getTitleVars());
      if (entry.getImage() != null) {
         label24: {
            Image image;
            try {
               image = SwingUtil.loadImage(entry.getImage());
            } catch (Exception var4) {
               this.log("could not load crash image", var4);
               break label24;
            }

            this.getHead().setIcon(new ImageIcon(image, SwingUtil.magnify(32), false));
         }
      } else {
         this.getHead().setIcon(this.crashIcon);
      }

      this.getHead().setText(entry.getTitle(), entry.getTitleVars());
      this.getBodyText().setText(entry.getBody(), entry.getBodyVars());
      ExtendedButton[] graphicsButtons = new ExtendedButton[entry.getButtons().size()];

      for(int i = 0; i < entry.getButtons().size(); ++i) {
         graphicsButtons[i] = ((Button)entry.getButtons().get(i)).toGraphicsButton(entry);
      }

      this.setButtons(entry.isPermitHelp(), graphicsButtons);
   }

   private void setButtons(boolean askHelp, ExtendedButton... buttons) {
      this.getFooter().removeAll();
      GridBagConstraints c = new GridBagConstraints();
      c.insets = SwingUtil.magnify(new Insets(0, 5, 0, 5));
      c.gridx = -1;
      c.weightx = 1.0D;
      c.weighty = 1.0D;
      c.anchor = 21;
      c.fill = 1;

      for(int i = 0; i < buttons.length; ++i) {
         ++c.gridx;
         buttons[i].setPreferredSize(new Dimension(buttons[i].getMinimumSize().width, SwingUtil.magnify(60)));
         this.getFooter().add(buttons[i], c);
      }

      ExtendedPanel buttonPanel = new ExtendedPanel();
      buttonPanel.setLayout(new GridBagLayout());
      GridBagConstraints c0 = new GridBagConstraints();
      c0.insets = SwingUtil.magnify(new Insets(2, 0, 2, 0));
      c0.gridx = 0;
      c0.gridy = -1;
      c0.weightx = 0.0D;
      c0.weighty = 1.0D;
      c0.anchor = 21;
      c0.fill = 1;
      if (askHelp) {
         ++c0.gridy;
         buttonPanel.add(this.askHelp, c0);
      }

      ++c0.gridy;
      buttonPanel.add(this.exitButton, c0);
      c.weightx = 0.0D;
      ++c.gridx;
      this.getFooter().add(buttonPanel, c);
   }

   private void log(Object... o) {
      U.log(this.logPrefix, o);
   }
}
