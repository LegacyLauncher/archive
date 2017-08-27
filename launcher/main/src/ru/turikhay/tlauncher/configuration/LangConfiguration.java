package ru.turikhay.tlauncher.configuration;

import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.U;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class LangConfiguration extends SimpleConfiguration {
    public static final Locale ru_RU = U.getLocale("ru_RU");

    private final Map<Locale, Properties> translationsMap = new HashMap<Locale, Properties>();
    private final Map<Locale, Pattern[]> pluralMap = new HashMap<Locale, Pattern[]>();

    private Pattern[] plurals;

    private Locale locale;

    public LangConfiguration() {
        setLocale(Locale.US);
    }

    public String nget(String key) {
        return key == null? null : properties.getProperty(key);
    }

    public String get(String key) {
        String value = nget(key);
        return value == null ? key : value;
    }

    public String nget(String key, Object... vars) {
        String value = nget(key);

        if (key == null || key.equals(value) || StringUtils.isEmpty(value)) {
            return null;
        }

        String[] variables = checkVariables(vars);

        if(plurals != null) {
            for (int var = 0; var < variables.length; var++) {
                String pluralReplacementValue = nget(key + '.' + var + ".plural");
                if (pluralReplacementValue == null) {
                    // plural is not found
                    value = StringUtils.replace(value, "%" + var, variables[var]);
                    continue;
                }

                String[] pluralReplacements = StringUtils.split(pluralReplacementValue, ';');

                for (int patternKey = 0; patternKey < plurals.length; patternKey++) {
                    if (plurals[patternKey].matcher(variables[var]).matches()) {
                        value = StringUtils.replace(value, "%" + var, StringUtils.replace(pluralReplacements[patternKey], "%n", variables[var]));
                        break;
                    }
                }
            }
        }

        return value;
    }

    public String get(String key, Object... vars) {
        String value = nget(key, vars);
        return value == null ? key : value;
    }

    public void set(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    private static String[] checkVariables(Object[] check) {
        if (check == null || check.length == 1 && check[0] == null) {
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

    public Locale getLocale() {
        return locale;
    }

    public synchronized void setLocale(Locale locale) {
        clear();

        this.locale = locale;
        this.plurals = null;

        if (locale == null) {
            log("WARNING: locale set to null");
            return;
        }

        Properties translations = getTranslations(locale);
        if (translations != null) {
            translationsMap.put(locale, translations);
            properties.putAll(translations);

            Pattern[] pluralPatterns = pluralMap.get(locale);
            if (pluralPatterns == null) {
                pluralMap.put(locale, pluralPatterns = getPluralPatterns(locale));
            }

            checkConsistancy(locale);

            this.plurals = pluralPatterns;
            log("Plurals for", locale, plurals);
        }
    }

    private Properties getTranslations(Locale locale) {
        if (locale == null) {
            return null;
        }

        Properties translations = translationsMap.get(locale);

        if (translations == null) {
            InputStream in = null;
            try {
                in = LangConfiguration.class.getResourceAsStream("/lang/lang_" + locale + ".properties");
                if (in == null) {
                    throw new NullPointerException("could not find translations for " + locale);
                }
                translations = loadFromStream(in);
            } catch (Exception e) {
                log("Could not load translations for:", locale, e);
                return null;
            } finally {
                U.close(in);
            }
        }

        return translations;
    }

    private Pattern[] getPluralPatterns(Locale locale) {
        Properties translations = getTranslations(locale);
        if (translations == null) {
            return null;
        }

        String pluralFormsRaw = translations.getProperty("plural");
        if (pluralFormsRaw == null) {
            log("Plural forms not found:", locale);
            return null;
        }

        String[] pluralFormsSplit = StringUtils.split(pluralFormsRaw, ';');
        Pattern[] pluralForms = new Pattern[pluralFormsSplit.length];

        for (int i = 0; i < pluralForms.length; i++) {
            try {
                pluralForms[i] = Pattern.compile(pluralFormsSplit[i]);
            } catch (PatternSyntaxException sE) {
                throw new IllegalArgumentException("\"" + pluralFormsSplit[i] + "\" is not valid pattern; check plural forms for " + locale, sE);
            }
        }

        return pluralForms;

    }

    private void checkConsistancy(Locale locale) {
        if (TLauncher.getInstance() == null || !TLauncher.getInstance().isDebug() || locale == ru_RU) {
            return;
        }

        Properties translations = getTranslations(locale);
        if(translations == null) {
            return;
        }

        Properties ruTranslations = getTranslations(ru_RU);
        if (ruTranslations != null) {
            for (Object key : ruTranslations.keySet()) {
                if (!translations.containsKey(key)) {
                    log(locale, "is missing key:", key);
                }
            }
            for (Object key : translations.keySet()) {
                if (!ruTranslations.containsKey(key)) {
                    log(locale, "has redundant key:", key);
                }
            }
        }
    }

    private static Locale getBackupLocaleFor(Locale locale) {
        if (locale == null || locale == Locale.US) {
            return null;
        }
        return Configuration.isUSSRLocale(locale.toString()) ? ru_RU : Locale.US;
    }

    private static final List<Locale> localeList;
    static {
        List<Locale> list = new ArrayList<Locale>(Static.getLangList().length);
        for(String locale : Static.getLangList()) {
            Locale l = U.getLocale(locale);
            if(l == null) {
                log(locale, "is not supported");
            } else {
                list.add(U.getLocale(locale));
            }
        }
        localeList = Collections.unmodifiableList(list);
    }

    public static List<Locale> getAvailableLocales() {
        return localeList;
    }

    private static void log(Object... o) {
        U.log("[Lang]", o);
    }
}
