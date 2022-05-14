package ru.turikhay.tlauncher.bootstrap.bridge;

import java.util.*;

public class FlatLafConfiguration {
    private static final String VERSION = "v1";

    private static final String PREFIX = "gui.laf."+ VERSION + ".";
    private static final String FLATLAF_PREFIX = PREFIX + "flatlaf.";

    public static final String KEY_STATE = PREFIX + "state";
    private static final String /* State.toString() */ DEFAULT_STATE = State.AUTO.toString();

    private static final String DARK_THEME_PREFIX_NO_DOT = FLATLAF_PREFIX + "dark";
    private static final String LIGHT_THEME_PREFIX_NO_DOT = FLATLAF_PREFIX + "light";

    private static final String KEY_DARK_THEME_FILE = DARK_THEME_PREFIX_NO_DOT;
    private static final String KEY_LIGHT_THEME_FILE = LIGHT_THEME_PREFIX_NO_DOT;
    private static final String DEFAULT_THEME_FILE = null;

    private static final String KEY_DARK_THEME_UI_PROPERTIES_FILE = DARK_THEME_PREFIX_NO_DOT + ".ui-properties-file";
    private static final String KEY_LIGHT_THEME_UI_PROPERTIES_FILE = LIGHT_THEME_PREFIX_NO_DOT + ".ui-properties-file";
    private static final String DEFAULT_UI_PROPERTIES_FILE = null;

    public static String getVersion() {
        return VERSION;
    }
    
    public static Map<String, String> getDefaults() {
        LinkedHashMap<String, String> defaults = new LinkedHashMap<>();
        defaults.put(KEY_STATE, DEFAULT_STATE);
        defaults.put(KEY_DARK_THEME_FILE, DEFAULT_THEME_FILE);
        defaults.put(KEY_LIGHT_THEME_FILE, DEFAULT_THEME_FILE);
        defaults.put(KEY_DARK_THEME_UI_PROPERTIES_FILE, DEFAULT_UI_PROPERTIES_FILE);
        defaults.put(KEY_LIGHT_THEME_UI_PROPERTIES_FILE, DEFAULT_UI_PROPERTIES_FILE);
        return defaults;
    }

    public static FlatLafConfiguration parseFromMap(Map<String, String> map) {
        return new FlatLafConfiguration(
                parseState(map.get(KEY_STATE)),
                map.getOrDefault(KEY_DARK_THEME_FILE, DEFAULT_THEME_FILE),
                map.getOrDefault(KEY_DARK_THEME_UI_PROPERTIES_FILE, DEFAULT_UI_PROPERTIES_FILE),
                map.getOrDefault(KEY_LIGHT_THEME_FILE, DEFAULT_THEME_FILE),
                map.getOrDefault(KEY_LIGHT_THEME_UI_PROPERTIES_FILE, DEFAULT_UI_PROPERTIES_FILE)
        );
    }

    private final State state;
    private final Map<Theme, String> themeFiles;
    private final Map<Theme, String> uiPropertiesFiles;

    public FlatLafConfiguration(
            State state,
            String darkThemeFile,
            String darkThemeUiPropertiesFile,
            String lightThemeFile,
            String lightThemeUiPropertiesFile
    ) {
        this.state = state;

        Map<Theme, String> themeConfig = new LinkedHashMap<>();
        themeConfig.put(Theme.DARK, darkThemeFile);
        themeConfig.put(Theme.LIGHT, lightThemeFile);
        this.themeFiles = Collections.unmodifiableMap(themeConfig);

        Map<Theme, String> uiPropertiesFiles = new LinkedHashMap<>();
        uiPropertiesFiles.put(Theme.DARK, darkThemeUiPropertiesFile);
        uiPropertiesFiles.put(Theme.LIGHT, lightThemeUiPropertiesFile);
        this.uiPropertiesFiles = Collections.unmodifiableMap(uiPropertiesFiles);
    }

    public boolean isEnabled() {
        return state != null && state.isEnabled();
    }

    public Optional<State> getState() {
        return Optional.ofNullable(state);
    }

    public Map<Theme, String> getThemeFiles() {
        return themeFiles;
    }

    public Map<Theme, String> getUiPropertiesFiles() {
        return uiPropertiesFiles;
    }

    public Optional<Theme> getSelected() {
        if (state != null) {
            switch (state) {
                case LIGHT:
                    return Optional.of(Theme.LIGHT);
                case DARK:
                    return Optional.of(Theme.DARK);
            }
        }
        return Optional.empty();
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(KEY_STATE, getState().map(State::toString).orElse(null));
        map.put(KEY_DARK_THEME_FILE, getThemeFiles().get(Theme.DARK));
        map.put(KEY_LIGHT_THEME_FILE, getThemeFiles().get(Theme.LIGHT));
        map.put(KEY_DARK_THEME_UI_PROPERTIES_FILE, getUiPropertiesFiles().get(Theme.DARK));
        map.put(KEY_LIGHT_THEME_UI_PROPERTIES_FILE, getUiPropertiesFiles().get(Theme.LIGHT));
        return map;
    }

    public enum State {
        OFF(false), SYSTEM(false), AUTO, DARK, LIGHT;

        private final boolean enabled;

        State(boolean enabled) {
            this.enabled = enabled;
        }

        State() {
            this(true);
        }

        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public String toString() {
            return super.name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Theme {
        DARK, LIGHT;

        @Override
        public String toString() {
            return super.name().toLowerCase(Locale.ROOT);
        }
    }

    private static State parseState(Object o) {
        if(o != null) {
            String v = o.toString();
            for (State state : State.values()) {
                if (state.toString().equals(v)) {
                    return state;
                }
            }
        }
        return null;
    }
}
