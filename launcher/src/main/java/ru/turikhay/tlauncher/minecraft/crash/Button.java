package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedButton;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Button {
    private static final Logger LOGGER = LogManager.getLogger(Button.class);

    private final List<Action> actions = new ArrayList<>();

    private final String name;

    private String text;
    private Object[] vars = Localizable.EMPTY_VARS;
    private boolean blockAfter, localizable = true, useGlobalPath = false;

    public Button(String name) {
        this.name = StringUtil.requireNotBlank(name, "name");
    }

    public final String getName() {
        return name;
    }

    public final String getText() {
        return text;
    }

    void setText(String text, Object... vars) {
        this.text = text;
        this.vars = vars == null ? Localizable.EMPTY_VARS : vars;
    }

    public final List<Action> getActions() {
        return actions;
    }

    void setActions(List<Action> actions) {
        this.actions.clear();
        this.actions.addAll(actions.stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public final boolean isBlockAfter() {
        return blockAfter;
    }

    void setBlockAfter(boolean blockAfter) {
        this.blockAfter = blockAfter;
    }

    public final boolean isLocalizable() {
        return localizable;
    }

    void setLocalizable(boolean localizable, boolean useGlobalPath) {
        this.localizable = localizable;
        this.useGlobalPath = useGlobalPath;
    }

    public ExtendedButton toGraphicsButton(final CrashEntry entry) {
        final ExtendedButton button;

        if (isLocalizable()) {
            button = new LocalizableButton(useGlobalPath ? "crash.buttons." + getText() : entry.getLocPath("buttons." + getText()), vars);
        } else {
            button = new ExtendedButton(getText());
        }

        FontMetrics metrics = button.getFontMetrics(button.getFont());
        int width = metrics.stringWidth(button.getText());
        Insets insets = button.getInsets();
        button.setMinimumSize(new Dimension(insets.left + width + insets.right, button.getHeight()));

        button.addActionListener(e -> {
            try {
                for (Action action : actions) {
                    action.execute();
                }
            } catch (Exception ex) {
                LOGGER.warn("Could not perform action", ex);
            }
            if (blockAfter) {
                button.setEnabled(false);
            }
        });
        return button;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("text", getText())
                .append("actions", getActions())
                .append("block", isBlockAfter())
                .append("loc", isLocalizable())
                .build();
    }

    public static class Deserializer implements JsonDeserializer<Button> {
        private final CrashManager manager;
        private final ITokenResolver resolver;

        public Deserializer(CrashManager manager, ITokenResolver resolver) {
            this.manager = Objects.requireNonNull(manager, "manager");
            this.resolver = resolver;
        }

        @Override
        public Button deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return deserialize(json, typeOfT, context, true);
        }

        public Button deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context, boolean useGlobalPath) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();

            String name;

            if (object.has("name")) {
                name = object.get("name").getAsString();
            } else {
                if (object.get("text").isJsonPrimitive()) {
                    name = object.get("text").getAsString();
                } else {
                    throw new JsonParseException("button has no name");
                }
            }

            Button button = new Button(name);
            button.setActions(getActions(object.get("actions"), context));

            if (object.get("text").isJsonPrimitive()) {
                button.setLocalizable(true, useGlobalPath);
                button.setText(object.get("text").getAsString());
            } else {
                button.setLocalizable(false, useGlobalPath);
                button.setText(CrashEntryList.getLoc(object.get("text"), context, resolver));
            }

            button.setBlockAfter(object.has("block") && object.get("block").getAsBoolean());

            return button;
        }

        private List<Action> getActions(JsonElement array, JsonDeserializationContext context) {
            if (array == null) {
                throw new NullPointerException();
            }

            if (!array.isJsonArray()) {
                throw new IllegalArgumentException();
            }

            JsonArray actions = array.getAsJsonArray();
            ArrayList<Action> actionList = new ArrayList<>(actions.size());

            for (int i = 0; i < actions.size(); i++) {
                JsonElement elem = actions.get(i);
                String str = elem.isJsonPrimitive() ? TokenReplacingReader.resolveVars(elem.getAsString(), resolver) : CrashEntryList.getLoc(elem, context, resolver);

                String[] splitAction = str.split(" ", 2);
                String name = splitAction[0], args = splitAction.length == 1 ? "" : splitAction[1];

                BindableAction action = manager.getAction(name);
                if (action == null) {
                    throw new NullPointerException("action " + name + " not found");
                }

                actionList.add(action.bind(args));
            }

            return actionList;
        }
    }
}
