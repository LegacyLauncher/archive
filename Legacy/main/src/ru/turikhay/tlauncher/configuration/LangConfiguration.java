package ru.turikhay.tlauncher.configuration;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LangConfiguration extends SimpleConfiguration {
    private static final char PLURAL_DELIMITER = ';';
    private static final String PLURAL_FLAG = "plural";

    private final Locale[] locales;
    private final Properties[] prop;
    private final Pattern[][] pluralPatterns;
    private int defI = -1, i;

    public LangConfiguration(Locale[] locales, Locale select) {
        this.locales = locales;

        int count = locales.length;
        prop = new Properties[count];

        for (int i = 0; i < count; i++) {
            Locale locale = locales[i];

            if (locale == null) {
                throw new NullPointerException("locale at " + i + " is null");
            }

            InputStream in = getClass().getResourceAsStream("/lang/" + locale + ".properties");
            if (in == null) {
                throw new RuntimeException("could not find file for: " + locale);
            }

            try {
                prop[i] = loadFromStream(in);
            } catch (IOException ioE) {
                throw new RuntimeException("could not load file for: " + locale, ioE);
            }

            if (locale.toString().equals("en_US")) {
                copyProperties(prop[i], properties, true);
            }
        }
        setSelected(select);

        pluralPatterns = new Pattern[count][0];

        int keyLocale = -1;
        for (int i = 0; i < count; i++) {
            if (locales[i].toString().equals("en_US")) {
                defI = i;
            }

            if (locales[i].toString().equals("ru_RU")) {
                keyLocale = i;
            }

            String pluralFormsValue = prop[i].getProperty("plural");
            if (pluralFormsValue == null) {
                continue;
            }

            String[] pluralForms = StringUtils.split(pluralFormsValue, PLURAL_DELIMITER);
            Pattern[] pluralFormsPatterns = new Pattern[pluralForms.length];

            for (int k = 0; k < pluralForms.length; k++) {
                try {
                    pluralFormsPatterns[k] = Pattern.compile(pluralForms[k]);
                } catch (PatternSyntaxException sE) {
                    throw new RuntimeException("could not compile plural form \"" + pluralForms[k] + "\" (index: " + k + ") for " + locales[i]);
                }
            }

            pluralPatterns[i] = pluralFormsPatterns;
            log("Plural patterns for " + locales[i] + ":", pluralFormsPatterns);
        }

        if (keyLocale != -1 && TLauncher.getInstance().getDebug()) {
            for (Object primaryKey : prop[keyLocale].keySet()) {

                for (int i = 0; i < count; i++) {
                    if (i == keyLocale) {
                        continue;
                    }
                    if (!prop[i].containsKey(primaryKey)) {
                        log("Missing key in " + locales[i] + ": " + primaryKey);
                    }
                }

            }

            for (int i = 0; i < count; i++) {
                if (i == keyLocale) {
                    continue;
                }
                for (Object redundantKey : prop[i].keySet()) {
                    if (!prop[keyLocale].containsKey(redundantKey)) {
                        log("Redundant key in " + locales[i] + ": " + redundantKey);
                        continue;
                    }

                    String key = redundantKey.toString();
                    if (key.endsWith("." + PLURAL_FLAG)) {
                        String pluralValues = prop[i].getProperty(key);
                        if (pluralValues == null) {
                            throw new NullPointerException("plural key is null: " + key + " in locale " + locales[i]);
                        }
                        String[] plurals = StringUtils.split(pluralValues, PLURAL_DELIMITER);
                        if (plurals.length != pluralPatterns[i].length) {
                            throw new RuntimeException("incorrect plural forms count: " + key + " in locale " + locales[i]);
                        }
                    }
                }
            }
        }
    }

    public Locale[] getLocales() {
        return locales;
    }

    public Locale getSelected() {
        return locales[i];
    }

    public void setSelected(Locale select) {
        if (select == null) {
            throw new NullPointerException();
        } else {
            boolean found = false;

            for (int i = 0; i < locales.length; ++i) {
                if (locales[i].equals(select)) {
                    this.i = i;
                    found = true;

                    break;
                }
            }

            if (!found)
                throw new IllegalArgumentException("Cannot find Locale:" + select);
        }
    }

    public String nget(String key) {
        return key == null ? null : prop[i].getProperty(key);
    }

    public String get(String key) {
        String value = nget(key);
        return value == null ? key : value;
    }

    public String nget(String key, Object... vars) {
        String value = nget(key);

        if (key == null ? value == null : (key.equals(value) || StringUtils.isEmpty(value))) {
            return null;
        }

        String[] variables = checkVariables(vars);

        for (int var = 0; var < variables.length; var++) {
            String pluralReplacementValue = nget(key + '.' + var + ".plural");
            if (pluralReplacementValue == null) {
                // plural is not found
                value = StringUtils.replace(value, "%" + var, variables[var]);
                continue;
            }

            String[] pluralReplacements = StringUtils.split(pluralReplacementValue, PLURAL_DELIMITER);

            for (int patternKey = 0; patternKey < pluralPatterns[i].length; patternKey++) {
                if (pluralPatterns[i][patternKey].matcher(variables[var]).matches()) {
                    value = StringUtils.replace(value, "%" + var, StringUtils.replace(pluralReplacements[patternKey], "%n", variables[var]));
                    break;
                }
            }
        }

        return value;
    }

    public String get(String key, Object... vars) {
        String value = nget(key, vars);
        return value == null ? key : value;
    }

    public String getDefault(String key) {
        if (defI == -1) {
            return null;
        }
        return prop[defI].getProperty(key);
    }

    public void set(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    private static String[] checkVariables(Object[] check) {
        if (check == null) {
            throw new NullPointerException();
        } else if (check.length == 1 && check[0] == null) {
            return new String[0];
        } else {
            String[] string = new String[check.length];

            for (int i = 0; i < check.length; ++i) {
                if (check[i] == null) {
                    throw new NullPointerException("Variable at index " + i + " is NULL!");
                }

                string[i] = check[i].toString();
            }

            return string;
        }
    }

    private static void log(Object... o) {
        U.log("[Lang]", o);
    }
}
