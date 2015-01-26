package ru.turikhay.tlauncher.ui.settings;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.Proxy.Type;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.editor.EditorIntegerRangeField;
import ru.turikhay.tlauncher.ui.editor.EditorTextField;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.VPanel;
import ru.turikhay.util.Range;
import ru.turikhay.util.U;

public class SettingsProxy extends VPanel implements EditorField {
   private static final String path = "settings.connection.proxy";
   private static final String block = "proxyselect";
   private final ExtendedPanel proxyTypePanel;
   private final LinkedHashMap typeMap = new LinkedHashMap();
   private final ButtonGroup group = new ButtonGroup();
   private final SettingsProxy.ProxySettingsPanel proxySettingsPanel;
   private final EditorTextField addressField;
   private final EditorTextField portField;

   SettingsProxy() {
      this.setAlignmentX(0.0F);
      List typeList = Arrays.asList(Type.values());
      Iterator var3 = typeList.iterator();

      while(var3.hasNext()) {
         Type type = (Type)var3.next();
         SettingsProxy.ProxyLocRadio radio = new SettingsProxy.ProxyLocRadio((SettingsProxy.ProxyLocRadio)null);
         radio.setText(type.name().toLowerCase());
         radio.setAlignmentX(0.0F);
         radio.setOpaque(false);
         this.group.add(radio);
         this.typeMap.put(type, radio);
      }

      ((SettingsProxy.ProxyLocRadio)this.typeMap.get(Type.DIRECT)).addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean selected = e.getStateChange() == 1;
            if (selected) {
               Blocker.block((Blockable)SettingsProxy.this.proxySettingsPanel, (Object)"proxyselect");
            }

         }
      });
      this.proxyTypePanel = new ExtendedPanel();
      this.proxyTypePanel.setAlignmentX(0.0F);
      this.add(this.proxyTypePanel);
      ItemListener listener = new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            boolean selected = e.getStateChange() == 1;
            if (selected) {
               Blocker.unblock((Blockable)SettingsProxy.this.proxySettingsPanel, (Object)"proxyselect");
            }

         }
      };
      Iterator var7 = this.typeMap.entrySet().iterator();

      while(var7.hasNext()) {
         Entry en = (Entry)var7.next();
         this.proxyTypePanel.add((Component)en.getValue());
         if (en.getKey() != Type.DIRECT) {
            ((SettingsProxy.ProxyLocRadio)en.getValue()).addItemListener(listener);
         }
      }

      this.proxySettingsPanel = new SettingsProxy.ProxySettingsPanel((SettingsProxy.ProxySettingsPanel)null);
      this.proxySettingsPanel.setAlignmentX(0.0F);
      this.add(this.proxySettingsPanel);
      this.addressField = new EditorTextField("settings.connection.proxy.address", false);
      this.proxySettingsPanel.setCenter(this.addressField);
      this.portField = new EditorIntegerRangeField("settings.connection.proxy.port", new Range(0, 65535));
      this.portField.setColumns(5);
      this.proxySettingsPanel.setEast(this.portField);
   }

   private Entry getSelectedType() {
      Iterator var2 = this.typeMap.entrySet().iterator();

      while(var2.hasNext()) {
         Entry en = (Entry)var2.next();
         if (((SettingsProxy.ProxyLocRadio)en.getValue()).isSelected()) {
            return en;
         }
      }

      return null;
   }

   private void setSelectedType(Type type) {
      Iterator var3 = this.typeMap.entrySet().iterator();

      while(var3.hasNext()) {
         Entry en = (Entry)var3.next();
         if (en.getKey() == type) {
            ((SettingsProxy.ProxyLocRadio)en.getValue()).setSelected(true);
            return;
         }
      }

      ((SettingsProxy.ProxyLocRadio)this.typeMap.get(Type.DIRECT)).setSelected(true);
   }

   public String getSettingsValue() {
      Entry selected = this.getSelectedType();
      if (selected != null && selected.getKey() != Type.DIRECT) {
         U.log(((Type)selected.getKey()).name().toLowerCase() + ';' + this.addressField.getValue() + ';' + this.portField.getValue());
         return ((Type)selected.getKey()).name().toLowerCase() + ';' + this.addressField.getValue() + ';' + this.portField.getValue();
      } else {
         U.log("selected is", selected, "so null");
         return null;
      }
   }

   public void setSettingsValue(String value) {
   }

   public boolean isValueValid() {
      Entry selected = this.getSelectedType();
      if (selected != null && selected.getKey() != Type.DIRECT) {
         return this.addressField.isValueValid() && this.portField.isValueValid();
      } else {
         return true;
      }
   }

   public void block(Object reason) {
      Blocker.blockComponents((Container)this, (Object)reason);
   }

   public void unblock(Object reason) {
      Blocker.unblockComponents((Container)this, (Object)reason);
   }

   private class ProxyLocRadio extends JRadioButton implements LocalizableComponent {
      private String currentType;

      private ProxyLocRadio() {
      }

      public void setText(String proxyType) {
         this.currentType = proxyType;
         String text = Localizable.get("settings.connection.proxy.type." + proxyType);
         if (StringUtils.isBlank(text)) {
            text = proxyType;
         }

         super.setText(text);
      }

      public void updateLocale() {
         this.setText(this.currentType);
      }

      // $FF: synthetic method
      ProxyLocRadio(SettingsProxy.ProxyLocRadio var2) {
         this();
      }
   }

   private class ProxySettingsPanel extends BorderPanel implements Blockable {
      private ProxySettingsPanel() {
      }

      public void block(Object reason) {
         Blocker.blockComponents((Container)this, (Object)reason);
      }

      public void unblock(Object reason) {
         Blocker.unblockComponents((Container)this, (Object)reason);
      }

      // $FF: synthetic method
      ProxySettingsPanel(SettingsProxy.ProxySettingsPanel var2) {
         this();
      }
   }
}
