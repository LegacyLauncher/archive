package ru.turikhay.tlauncher.ui.support;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import ru.turikhay.tlauncher.ui.frames.ProcessFrame;
import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

public class PreSupportFrame extends VActionFrame {
   private final ExtendedLabel whatIsDiagnosticLabel = new ExtendedLabel();
   private final LocalizableCheckbox sendDiagnosticCheckbox;
   private final ExtendedPanel checkboxPanel;
   private final SendInfoFrame sendInfoFrame;
   private final SupportFrame[] supportFrames;
   private final LocalizableButton[] supportFramesButtons;

   public PreSupportFrame() {
      this.whatIsDiagnosticLabel.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent e) {
            OS.openLink(Localizable.get("support.pre.diag.url"));
         }
      });
      this.whatIsDiagnosticLabel.setIcon(Images.getIcon("lightbulb.png", SwingUtil.magnify(16)));
      this.whatIsDiagnosticLabel.setCursor(Cursor.getPredefinedCursor(12));
      this.sendDiagnosticCheckbox = new LocalizableCheckbox("support.pre.diag.checkbox");
      this.sendDiagnosticCheckbox.setSelected(true);
      this.checkboxPanel = new ExtendedPanel();
      this.checkboxPanel.setInsets(new Insets(0, 0, 0, 0));
      FlowLayout layout = new FlowLayout(0);
      layout.setHgap(0);
      layout.setVgap(0);
      this.checkboxPanel.setLayout(layout);
      this.checkboxPanel.add(this.sendDiagnosticCheckbox);
      this.checkboxPanel.add(this.whatIsDiagnosticLabel);
      this.sendInfoFrame = new SendInfoFrame() {
         protected void onSucceeded(ProcessFrame.Process process, SendInfoFrame.SendInfoResponse result) {
            super.onSucceeded(process, result);
            PreSupportFrame.this.setVisible(false);
         }

         protected void onFailed(ProcessFrame.Process process, Exception e) {
            super.onFailed(process, e);
            PreSupportFrame.this.sendDiagnosticCheckbox.setSelected(false);
         }
      };
      this.supportFrames = new SupportFrame[]{new VkSupportFrame(), new FbSupportFrame(), new MailSupportFrame()};
      this.supportFramesButtons = new LocalizableButton[this.supportFrames.length];

      for(int i = 0; i < this.supportFrames.length; ++i) {
         final SupportFrame frame = this.supportFrames[i];
         LocalizableButton button = this.supportFramesButtons[i] = new LocalizableButton("support.pre.buttons." + frame.name);
         button.setIcon(Images.getIcon(frame.getImage(), SwingUtil.magnify(16)));
         button.setPreferredSize(new Dimension(1, SwingUtil.magnify(50)));
         button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               PreSupportFrame.this.onSupportFrameSelected(frame);
            }
         });
      }

      this.addComponentListener(new ComponentAdapter() {
         public void componentShown(ComponentEvent e) {
            PreSupportFrame.this.whatIsDiagnosticLabel.requestFocus();
         }
      });
      this.setTitlePath("support.pre.title", new Object[0]);
      this.getHead().setIcon(Images.getIcon("consulting.png", SwingUtil.magnify(32)));
      this.getHead().setText("support.pre.title");
      this.getBodyText().setText("support.pre.body");
      this.getFooter().setLayout(new GridBagLayout());
      this.getFooter().removeAll();
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 1.0D;
      c.gridwidth = 0;
      c.anchor = 21;
      c.fill = 2;
      this.getFooter().add(this.checkboxPanel, c);
      c = new GridBagConstraints();
      c.gridx = -1;
      c.gridy = 1;
      c.weightx = 1.0D;
      c.weighty = 1.0D;
      c.fill = 1;

      for(int i = 0; i < this.supportFrames.length; ++i) {
         if (this.supportFrames[i].isApplicable()) {
            this.getFooter().add(this.supportFramesButtons[i], c);
         }
      }

      this.pack();
      this.whatIsDiagnosticLabel.setToolTipText(Localizable.get("support.pre.diag.whatisit"));
   }

   protected void onSupportFrameSelected(SupportFrame frame) {
      if (this.sendDiagnosticCheckbox.isSelected()) {
         this.sendInfoFrame.setFrame(frame);
      } else {
         frame.openUrl();
      }

   }
}
