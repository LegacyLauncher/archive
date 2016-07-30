package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

public class Button {
   private final List actions = new ArrayList();
   private final String name;
   private String text;
   private Object[] vars;
   private boolean blockAfter;
   private boolean localizable = true;
   private boolean useGlobalPath = false;

   public Button(String name) {
      this.name = StringUtil.requireNotBlank(name, "name");
   }

   public final String getName() {
      return this.name;
   }

   public final String getText() {
      return this.text;
   }

   void setText(String text, Object... vars) {
      this.text = text;
      this.vars = vars;
   }

   public final List getActions() {
      return this.actions;
   }

   void setActions(List actions) {
      this.actions.clear();
      this.actions.addAll(U.requireNotContainNull(actions, "actions"));
   }

   public final boolean isBlockAfter() {
      return this.blockAfter;
   }

   void setBlockAfter(boolean blockAfter) {
      this.blockAfter = blockAfter;
   }

   public final boolean isLocalizable() {
      return this.localizable;
   }

   void setLocalizable(boolean localizable, boolean useGlobalPath) {
      this.localizable = localizable;
      this.useGlobalPath = useGlobalPath;
   }

   public ExtendedButton toGraphicsButton(final CrashEntry entry) {
      final Object button;
      if (this.isLocalizable()) {
         button = new LocalizableButton(this.useGlobalPath ? "crash.buttons." + this.getText() : entry.getLocPath("buttons." + this.getText()), (Object[])this.vars);
      } else {
         button = new ExtendedButton(this.getText());
      }

      FontMetrics metrics = ((ExtendedButton)button).getFontMetrics(((ExtendedButton)button).getFont());
      int width = metrics.stringWidth(((ExtendedButton)button).getText());
      Insets insets = ((ExtendedButton)button).getInsets();
      ((ExtendedButton)button).setMinimumSize(new Dimension(insets.left + width + insets.right, ((ExtendedButton)button).getHeight()));
      ((ExtendedButton)button).addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            try {
               Iterator var2 = Button.this.actions.iterator();

               while(var2.hasNext()) {
                  Action action = (Action)var2.next();
                  action.execute();
               }
            } catch (Exception var4) {
               entry.log(new Object[]{var4});
            }

            if (Button.this.blockAfter) {
               ((ExtendedButton)button).setEnabled(false);
            }

         }
      });
      return (ExtendedButton)button;
   }

   public String toString() {
      return (new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).append("text", this.getText()).append("actions", this.getActions()).append("block", this.isBlockAfter()).append("loc", this.isLocalizable()).build();
   }

   public static class Deserializer implements JsonDeserializer {
      private final CrashManager manager;
      private final ITokenResolver resolver;

      public Deserializer(CrashManager manager, ITokenResolver resolver) {
         this.manager = (CrashManager)U.requireNotNull(manager, "manager");
         this.resolver = resolver;
      }

      public Button deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         return this.deserialize(json, typeOfT, context, true);
      }

      public Button deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context, boolean useGlobalPath) throws JsonParseException {
         JsonObject object = json.getAsJsonObject();
         String name;
         if (object.has("name")) {
            name = object.get("name").getAsString();
         } else {
            if (!object.get("text").isJsonPrimitive()) {
               throw new JsonParseException("button has no name");
            }

            name = object.get("text").getAsString();
         }

         Button button = new Button(name);
         button.setActions(this.getActions(object.get("actions"), context));
         if (object.get("text").isJsonPrimitive()) {
            button.setLocalizable(true, useGlobalPath);
            button.setText(object.get("text").getAsString());
         } else {
            button.setLocalizable(false, useGlobalPath);
            button.setText(CrashEntryList.getLoc(object.get("text"), context, this.resolver));
         }

         button.setBlockAfter(object.has("block") && object.get("block").getAsBoolean());
         return button;
      }

      private List getActions(JsonElement array, JsonDeserializationContext context) {
         if (array == null) {
            throw new NullPointerException();
         } else if (!array.isJsonArray()) {
            throw new IllegalArgumentException();
         } else {
            JsonArray actions = array.getAsJsonArray();
            ArrayList actionList = new ArrayList(actions.size());

            for(int i = 0; i < actions.size(); ++i) {
               JsonElement elem = actions.get(i);
               String str = elem.isJsonPrimitive() ? TokenReplacingReader.resolveVars(elem.getAsString(), this.resolver) : CrashEntryList.getLoc(elem, context, this.resolver);
               String[] splitAction = str.split(" ", 2);
               String name = splitAction[0];
               String args = splitAction.length == 1 ? "" : splitAction[1];
               BindableAction action = this.manager.getAction(name);
               if (action == null) {
                  throw new NullPointerException("action " + name + " not found");
               }

               actionList.add(action.bind(args));
            }

            return actionList;
         }
      }
   }
}
