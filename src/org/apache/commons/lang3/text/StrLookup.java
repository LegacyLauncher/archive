package org.apache.commons.lang3.text;

import java.util.Map;

public abstract class StrLookup<V> {
    private static final StrLookup<String> NONE_LOOKUP;
    private static final StrLookup<String> SYSTEM_PROPERTIES_LOOKUP;
    static {
        NONE_LOOKUP = new MapStrLookup<String>(null);
        StrLookup<String> lookup = null;
        try {
            final Map<?, ?> propMap = System.getProperties();
            @SuppressWarnings("unchecked") // System property keys and values are always Strings
            final Map<String, String> properties = (Map<String, String>) propMap;
            lookup = new MapStrLookup<String>(properties);
        } catch (SecurityException ex) {
            lookup = NONE_LOOKUP;
        }
        SYSTEM_PROPERTIES_LOOKUP = lookup;
    }
    
    public static StrLookup<?> noneLookup() {
        return NONE_LOOKUP;
    }

    public static StrLookup<String> systemPropertiesLookup() {
        return SYSTEM_PROPERTIES_LOOKUP;
    }

    public static <V> StrLookup<V> mapLookup(Map<String, V> map) {
        return new MapStrLookup<V>(map);
    }

    protected StrLookup() {
        super();
    }

    public abstract String lookup(String key);
    
    static class MapStrLookup<V> extends StrLookup<V> {

        private final Map<String, V> map;

        MapStrLookup(Map<String, V> map) {
            this.map = map;
        }

        @Override
        public String lookup(String key) {
            if (map == null) {
                return null;
            }
            Object obj = map.get(key);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }
    }
}
