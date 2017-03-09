package ru.turikhay.tlauncher.minecraft.crash;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.util.OS;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.MapTokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;
import ru.turikhay.util.json.LegacyVersionSerializer;

import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

public final class CrashEntryList {
    private final List<CrashEntry> signatures = new ArrayList<CrashEntry>(), _signatures = Collections.unmodifiableList(signatures);
    private int revision;
    private Version required;

    private CrashEntryList() {
    }

    public List<CrashEntry> getSignatures() {
        return _signatures;
    }

    public int getRevision() {
        return revision;
    }

    public Version getRequired() {
        return required;
    }

    public static class ListDeserializer implements JsonDeserializer<CrashEntryList> {
        private final LegacyVersionSerializer versionSerializer = new LegacyVersionSerializer();

        private final Map<String, String> vars;
        private final Map<String, Button> buttonMap;

        private final MapTokenResolver varsResolver;
        private final Button.Deserializer buttonDeserializer;

        private final CrashManager manager;

        ListDeserializer(CrashManager manager) {
            this.manager = U.requireNotNull(manager);

            vars = new LinkedHashMap<String, String>();
            vars.put("os", OS.CURRENT.toString());
            vars.put("arch", OS.Arch.CURRENT.toString());
            vars.put("locale", TLauncher.getInstance().getSettings().getLocale().toString());

            varsResolver = new MapTokenResolver(vars);

            buttonMap = new HashMap<String, Button>();
            buttonDeserializer = new Button.Deserializer(manager, varsResolver);
        }

        Map<String, String> getVars() {
            return vars;
        }

        Map<String, Button> getButtons() {
            return buttonMap;
        }

        public String asString(JsonElement elem) {
            return TokenReplacingReader.resolveVars(elem.getAsString(), varsResolver);
        }

        @Override
        public CrashEntryList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject root = json.getAsJsonObject();

            Version required = versionSerializer.deserialize(root.get("required"), Version.class, context);
            if (TLauncher.getVersion().lessThan(required)) {
                throw new IncompatibleEntryList(required);
            }

            CrashEntryList entryList = new CrashEntryList();
            entryList.required = required;
            entryList.revision = root.get("revision").getAsInt();

            HashMap<String, String> loadedVars = new HashMap<String, String>();
            for (Map.Entry<String, JsonElement> entry : root.get("variables").getAsJsonObject().entrySet()) {
                String value;
                loadedVars.put(entry.getKey(), value = getLoc(entry.getValue(), context, varsResolver));
                log("Processing var:", entry.getKey(), value);
            }
            vars.putAll(loadedVars);

            JsonArray buttons = root.get("buttons").getAsJsonArray();
            for (int i = 0; i < buttons.size(); i++) {
                Button button = buttonDeserializer.deserialize(buttons.get(i), Button.class, context, true);
                buttonMap.put(button.getName(), button);
                //log("Added button:", button);
            }

            EntryDeserializer entryDeserializer = new EntryDeserializer(buttonMap, buttonDeserializer);
            JsonArray signatures = root.get("signatures").getAsJsonArray();
            for (int i = 0; i < signatures.size(); i++) {
                CrashEntry entry = entryDeserializer.deserialize(signatures.get(i), EntryDeserializer.class, context);
                //log("Entry parsed:", entry.getName());
                entryList.signatures.add(entry);
            }

            log(entryList.signatures.size(), "crash entries were parsed");


            Collections.sort(entryList.signatures, new Comparator<CrashEntry>() {
                @Override
                public int compare(CrashEntry entry1, CrashEntry entry2) {
                    boolean r1 = entry1.requiresDxDiag(), r2 = entry2.requiresDxDiag();
                    if(r1 == r2) {
                        return 0;
                    }
                    return r1? 1 : -1;
                }
            });

            return entryList;
        }

        public class EntryDeserializer implements JsonDeserializer<CrashEntry> {
            private final Map<String, Button> buttonMap;
            private final Button.Deserializer buttonDeserializer;

            private EntryDeserializer(Map<String, Button> buttonMap, Button.Deserializer buttonDeserializer) {
                this.buttonMap = buttonMap;
                this.buttonDeserializer = buttonDeserializer;
            }

            @Override
            public CrashEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = json.getAsJsonObject();

                CrashEntry entry;
                String name = object.get("name").getAsString();

                if (object.has("pattern")) {
                    entry = new PatternEntry(manager, name, Pattern.compile(asString(object.get("pattern"))));
                } else {
                    entry = new CrashEntry(manager, name);
                }

                if (object.has("fake")) {
                    entry.setFake(object.get("fake").getAsBoolean());
                }

                if (object.has("permitHelp")) {
                    entry.setPermitHelp(object.get("permitHelp").getAsBoolean());
                }

                if (object.has("exitCode")) {
                    entry.setExitCode(object.get("exitCode").getAsInt());
                }

                if (object.has("version")) {
                    entry.setVersionPattern(Pattern.compile(asString(object.get("version"))));
                }

                if (object.has("jre")) {
                    entry.setJrePattern(Pattern.compile(asString(object.get("jre"))));
                }

                if (object.has("archIssue")) {
                    entry.setArchIssue(object.get("archIssue").getAsBoolean());
                }

                if (object.has("graphicsCard")) {
                    entry.setGraphicsCardPattern(Pattern.compile(asString(object.get("graphicsCard"))));
                }

                if (object.has("title")) {
                    entry.setTitle(getLoc(object.get("title"), context, varsResolver));
                }

                if (object.has("loc")) {
                    entry.setLocalizable(object.get("loc").getAsBoolean());
                }

                if (object.has("body")) {
                    entry.setBody(getLoc(object.get("body"), context, varsResolver));
                }

                if (object.has("image")) {
                    entry.setImage(asString(object.get("image")));
                }

                if (object.has("os")) {
                    entry.setOS((OS[]) context.deserialize(object.get("os"), new TypeToken<OS[]>() {
                    }.getType()));
                }

                if (object.has("buttons")) {
                    JsonArray buttons = object.getAsJsonArray("buttons");
                    for (int i = 0; i < buttons.size(); i++) {
                        JsonElement elem = buttons.get(i);
                        Button button;

                        if (elem.isJsonPrimitive()) {
                            button = buttonMap.get(elem.getAsString());
                        } else {
                            button = buttonDeserializer.deserialize(buttons.get(i), Button.class, context, false);
                        }

                        entry.addButton(button);
                    }
                }

                return entry;
            }
        }

        private void log(Object... o) {
            U.log("[CrashEntryDeserializer]", o);
        }

        class IncompatibleEntryList extends JsonParseException {
            IncompatibleEntryList(Version required) {
                super("required: " + required);
            }
        }
    }

    public static String getLoc(JsonElement elem, JsonDeserializationContext context, ITokenResolver resolver) {
        if (elem == null) {
            throw new NullPointerException();
        }

        if (elem.isJsonPrimitive()) {
            return TokenReplacingReader.resolveVars(elem.getAsString(), resolver);
        }

        JsonObject obj = elem.getAsJsonObject();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        Map<String, String> map = context.deserialize(obj, type);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            U.requireNotNull(entry.getKey());
            StringUtil.requireNotBlank(entry.getValue());
        }

        String locale = (TLauncher.getInstance() == null ? "en_US" : TLauncher.getInstance().getLang().getLocale()).toString();
        if (!map.containsKey(locale)) {
            if (Configuration.isUSSRLocale(locale) && map.containsKey("ru_RU")) {
                locale = "ru_RU";
            } else {
                locale = "en_US";
            }
        }
        return map.get(locale);
    }
}
