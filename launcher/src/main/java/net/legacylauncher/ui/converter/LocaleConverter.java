package net.legacylauncher.ui.converter;

import net.legacylauncher.util.U;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class LocaleConverter implements StringConverter<Locale> {
    public String toString(Locale from, Locale format) {
        if (from == null) {
            return null;
        }
        String displayLang;
        try {
            displayLang = from.getDisplayLanguage(format);
            if (StringUtils.isEmpty(displayLang))
                throw new IllegalArgumentException();
        } catch (Exception e) {
            displayLang = from.getDisplayLanguage(Locale.US);
        }
        return displayLang + " (" + from + ")";
    }

    public String toString(Locale from) {
        return toString(from, from);
    }

    public Locale fromString(String from) {
        return U.getLocale(from);
    }

    public String toValue(Locale from) {
        return from == null ? null : from.toString();
    }

    public Class<Locale> getObjectClass() {
        return Locale.class;
    }
}
