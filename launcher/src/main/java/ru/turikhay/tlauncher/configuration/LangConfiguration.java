package ru.turikhay.tlauncher.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.U;

import java.io.Console;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LangConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Locale ru_RU = U.getLocale("ru_RU");

    private final Map<Locale, Properties> translationsMap = new HashMap<>();
    private final Map<Locale, Pattern[]> pluralMap = new HashMap<>();

    private Pattern[] plurals;

    private Locale locale;

    public LangConfiguration() {
        setLocale(Locale.US);
    }

    public String lget(Locale locale, String key) {
        if (key == null) {
            return null;
        }
        if (translationsMap.containsKey(locale)) {
            Properties l = translationsMap.get(locale);
            return l.getProperty(key);
        }
        return null;
    }

    public String lget(Locale locale, String key, Object... vars) {
        if (key == null) {
            return null;
        }

        String value = lget(locale, key);
        if (key.equals(value) || StringUtils.isEmpty(value)) {
            return null;
        }

        String[] variables = checkVariables(vars);

        if (pluralMap.containsKey(locale)) {
            Pattern[] plurals = pluralMap.get(locale);

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

    public String nget(String key) {
        return lget(locale, key);
    }

    public String get(String key) {
        String value = nget(key);
        if (value == null) {
            value = lget(selectBackingLocale(), key);
        }
        return value == null ? key : value;
    }

    public String nget(String key, Object... vars) {
        return lget(locale, key, vars);
    }

    public String get(String key, Object... vars) {
        String value = nget(key, vars);
        if (value == null) {
            value = lget(selectBackingLocale(), key, vars);
        }
        return value == null ? key : value;
    }

    private Locale selectBackingLocale() {
        if (locale != ru_RU && Configuration.isLikelyRussianSpeakingLocale(locale.toString())) {
            loadLocale(ru_RU);
            return ru_RU;
        } else {
            return Locale.US;
        }
    }

    private static String[] checkVariables(Object[] check) {
        if (check == null || check.length == 1 && check[0] == null) {
            return new String[0];
        } else {
            String[] string = new String[check.length];

            for (int i = 0; i < check.length; ++i) {
                string[i] = String.valueOf(check[i]);
            }

            return string;
        }
    }

    public Locale getLocale() {
        return locale;
    }

    public synchronized void setLocale(Locale locale) {
        this.locale = locale;
        this.plurals = null;

        if (locale == null) {
            LOGGER.warn("Tried to set locale to null");
            return;
        }

        loadLocale(locale);
    }

    private synchronized void loadLocale(Locale locale) {
        Properties translations = getTranslations(locale);
        if (translations != null) {
            translationsMap.put(locale, translations);

            Pattern[] pluralPatterns = pluralMap.get(locale);
            if (pluralPatterns == null) {
                pluralMap.put(locale, pluralPatterns = getPluralPatterns(locale));
            }

            checkConsistancy(locale);

            this.plurals = pluralPatterns;
        }
    }

    private Properties getTranslations(Locale localeObj) {
        if (localeObj == null) {
            return null;
        }

        String localeStr = localeObj.toString();
        if (localeStr.equals("id_ID")) {
            localeStr = "in_ID";
        }

        Properties translations = translationsMap.get(localeObj);

        if (translations == null) {

            try (InputStream in = LangConfiguration.class.getResourceAsStream("/lang/lang_" + localeStr + ".properties")) {
                if (in == null) {
                    throw new NullPointerException("could not find translations for " + localeStr);
                }
                translations = SimpleConfiguration.loadFromStream(in);
            } catch (Exception e) {
                LOGGER.warn("Could not load translations for {}", localeStr, e);
                return null;
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
            LOGGER.warn("Plural forms not found: {}", locale);
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
        if (translations == null) {
            return;
        }

        Properties ruTranslations = getTranslations(ru_RU);
        if (ruTranslations != null) {
            for (Object key : ruTranslations.keySet()) {
                if (!translations.containsKey(key)) {
                    LOGGER.warn("{} is missing key: {}", locale, key);
                }
            }
            for (Object key : translations.keySet()) {
                if (!ruTranslations.containsKey(key)) {
                    LOGGER.warn("{} has redundant key: {}", locale, key);
                }
            }
        }
    }

    private static final Lazy<List<Locale>> localeList = Lazy.of(() -> {
        URL url = LangConfiguration.class.getResource("/lang");
        if (url == null) {
            LOGGER.fatal("No available locales");
            return Collections.emptyList();
        }

        final List<Locale> locales;
        final Function<Stream<Path>, List<Locale>> locator = stream -> stream.map(it -> it.getFileName().toString())
                .filter(it -> it.startsWith("lang_") && it.endsWith(".properties"))
                .map(it -> it.substring("lang_".length(), it.length() - ".properties".length()).replace('_', '-'))
                .map(Locale::forLanguageTag)
                .sorted(LocaleComparator.INSTANCE)
                .collect(Collectors.toList());

        URI uri = url.toURI();
        if (uri.getScheme().equals("jar")) {
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap(), LangConfiguration.class.getClassLoader())) {
                try (Stream<Path> stream = Files.walk(fs.getPath("/lang"), 1).skip(1)) {
                    locales = locator.apply(stream);
                }
            }
        } else {
            try (Stream<Path> stream = Files.walk(Paths.get(uri), 1).skip(1)) {
                locales = locator.apply(stream);
            }
        }
        return Collections.unmodifiableList(locales);
    });

    public static List<Locale> getAvailableLocales() {
        return localeList.get();
    }

    private static class LocaleComparator implements Comparator<Locale> {
        public static final Comparator<Locale> INSTANCE = new LocaleComparator();

        @Override
        public int compare(Locale o1, Locale o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }
}
