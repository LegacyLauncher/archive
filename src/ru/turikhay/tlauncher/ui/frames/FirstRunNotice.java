package ru.turikhay.tlauncher.ui.frames;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Locale;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.converter.LocaleConverter;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComboBox;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.SwingUtil;

public class FirstRunNotice extends ActionFrame {
   private final TLauncher t = TLauncher.getInstance();

   public FirstRunNotice() {
      super(new Dimension(500, 335));
      this.setTitlePath("firstrun.title");
      this.setDefaultCloseOperation(3);
      this.getHead().setText("firstrun.notice.welcome");
      VPanel list = new VPanel();
      list.setInsets(new MagnifiedInsets(0, 10, 0, 0));

      for(int i = 0; i < 3; ++i) {
         list.add(new LocalizableHTMLLabel("firstrun.notice.body." + i));
      }

      this.getBodyText().setText("firstrun.notice.body");
      this.getBody().add(list);
      this.getFooter().setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = -1;
      ++c.gridx;
      c.weightx = 0.0D;
      c.fill = 0;
      final ExtendedComboBox localeChoose = new ExtendedComboBox(new LocaleConverter() {
         public String toString(Locale from) {
            return this.toString(from, Locale.US);
         }
      });
      Locale[] var4 = this.t.getLang().getLocales();
      int var5 = var4.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Locale locale = var4[var6];
         localeChoose.addItem(locale);
      }

      localeChoose.setSelectedValue(this.t.getLang().getSelected());
      localeChoose.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            Locale selected = (Locale)localeChoose.getSelectedValue();
            if (selected == null) {
               selected = Locale.US;
            }

            FirstRunNotice.this.t.getSettings().set("locale", selected);
            FirstRunNotice.this.t.getLang().setSelected(selected);
            FirstRunNotice.this.updateLocale();
         }
      });
      this.getFooter().add(localeChoose, c);
      ++c.gridx;
      c.weightx = 1.0D;
      c.fill = 2;
      this.getFooter().add(new ExtendedPanel(), c);
      ++c.gridx;
      c.weightx = 0.0D;
      c.fill = 0;
      LocalizableButton yesButton = new LocalizableButton("firstrun.notice.answer.yes");
      yesButton.setPreferredSize(SwingUtil.magnify(new Dimension(150, 40)));
      yesButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            FirstRunNotice.this.dispose();
         }
      });
      this.getFooter().add(yesButton, c);
      this.updateLocale();
   }
}
