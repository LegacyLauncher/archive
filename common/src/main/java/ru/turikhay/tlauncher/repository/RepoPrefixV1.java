package ru.turikhay.tlauncher.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RepoPrefixV1 {
    private static final Logger LOGGER = LoggerFactory.getLogger(RepoPrefixV1.class);

    private static final List<String> PREFIXES;
    private static final List<String> CDN_PREFIXES;

    static {
        final Properties props = new Properties();
        try (InputStreamReader reader = new InputStreamReader(
                Objects.requireNonNull(RepoPrefixV1.class.getResourceAsStream("repositories_v1.properties")),
                StandardCharsets.UTF_8
        )) {
            props.load(reader);
        } catch (Exception e) {
            LOGGER.warn("Couldn't load internal repo list: ", e);
        }
        final List<String> domains = getList(props, "domains", Arrays.asList("llaun.ch", "lln4.ru"));
        List<String> prefixes = combine(
                shuffle(
                        getList(props, "eu_prefixes", Collections.singletonList("eu1"))
                ),
                shuffle(
                        getList(props, "ru_prefixes", Collections.singletonList("ru1"))
                )
        ).stream().flatMap(zone ->
                        Stream.of(
                                domains.stream().map(domain -> String.format(Locale.ROOT, "https://%s", domain)),
                                domains.stream().map(domain ->
                                        String.format(Locale.ROOT, "https://%s.%s", zone, domain)
                                )
                        ).flatMap(
                                Function.identity()
                        )
        ).collect(
                Collectors.toList()
        );
        CDN_PREFIXES = getList(props, "cdn_prefixes", Collections.singletonList("https://cdn.turikhay.ru/lln4"));
        PREFIXES = Collections.unmodifiableList(prefixes);
    }

    public static List<String> prefixesCdnFirst() {
        return Collections.unmodifiableList(
                combine(
                        CDN_PREFIXES,
                        PREFIXES
                )
        );
    }

    public static List<String> prefixesCdnLast() {
        return Collections.unmodifiableList(
                combine(
                        PREFIXES,
                        CDN_PREFIXES
                )
        );
    }

    public static List<String> prefixes() {
        return PREFIXES;
    }

    public static List<String> cdnPrefixes() {
        return CDN_PREFIXES;
    }

    private static <T> List<T> shuffle(List<T> list) {
        Collections.shuffle(list);
        return list;
    }

    private static List<String> getList(Properties properties, String key, List<String> fallbackValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return fallbackValue;
        }
        return Arrays.asList(value.split(","));
    }

    @SafeVarargs
    public static List<String> combine(List<String>... lists) {
        ArrayList<String> combined = new ArrayList<>();
        for (List<String> list : lists) {
            combined.addAll(list);
        }
        return combined;
    }
}
