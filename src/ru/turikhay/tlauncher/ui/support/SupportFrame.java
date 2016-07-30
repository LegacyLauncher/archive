package ru.turikhay.tlauncher.ui.support;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ru.turikhay.tlauncher.ui.frames.VActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.tlauncher.ui.text.ExtendedTextField;
import ru.turikhay.util.OS;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public abstract class SupportFrame extends VActionFrame {
   protected final String name;
   private final ExtendedTextField textField;
   private final LocalizableButton openButton;
   private final String image;
   private final String url;

   SupportFrame(String name, String image, String url) {
      this.name = StringUtil.requireNotBlank(name, "name");
      this.url = StringUtil.requireNotBlank(url, "url");
      this.getFooter().setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.insets = new MagnifiedInsets(5, 0, 5, 0);
      c.gridx = 0;
      c.gridy = -1;
      c.weightx = 1.0D;
      c.fill = 2;
      LocalizableLabel textLabel = new LocalizableLabel("support." + name + ".code");
      ++c.gridy;
      this.getFooter().add(textLabel, c);
      this.textField = new ExtendedTextField();
      this.textField.setFont(this.textField.getFont().deriveFont(this.textField.getFont().getSize2D() + 4.0F));
      this.textField.setEditable(false);
      this.textField.addMouseListener(new TextPopup());
      ++c.gridy;
      this.getFooter().add(this.textField, c);
      this.openButton = new LocalizableButton();
      this.openButton.setPreferredSize(SwingUtil.magnify(new Dimension(1, 40)));
      this.openButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            SupportFrame.this.onOpenButtonClicked();
         }
      });
      ++c.gridy;
      this.getFooter().add(this.openButton, c);
      this.setTitlePath("support.title", new Object[0]);
      this.getHead().setText("support.title");
      this.getHead().setIcon(Images.getIcon(this.image = image, SwingUtil.magnify(32)));
      this.getBodyText().setText("support." + name + ".body");
      this.getOpenButton().setText("support." + name + ".open");
      this.pack();
   }

   void setResponse(SendInfoFrame.SendInfoResponse response) {
      U.requireNotNull(response);
      this.getTextField().setText(this.getCode(response));
      this.showAtCenter();
   }

   String getImage() {
      return this.image;
   }

   ExtendedTextField getTextField() {
      return this.textField;
   }

   LocalizableButton getOpenButton() {
      return this.openButton;
   }

   void onOpenButtonClicked() {
      SwingUtil.setClipboard(this.textField.getValue());
      this.openUrl();
   }

   boolean isApplicable() {
      return true;
   }

   String getCode(SendInfoFrame.SendInfoResponse response) {
      return response.getPastebinLink();
   }

   public void openUrl() {
      OS.openLink(this.url);
   }
}
