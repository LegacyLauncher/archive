package ru.turikhay.util.json;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyVersionSerializer extends VersionSerializer {
    private final Pattern legacyVersionPattern = Pattern.compile("([\\d])\\.([\\d])([\\d]?)([\\d]*)");

    @Override
    public Version deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        double val;

        if (json == null) {
            return super.deserialize(null, typeOfT, context);
        }

        try {
            val = json.getAsDouble();
        } catch (NumberFormatException nfE) {
            return super.deserialize(json, typeOfT, context);
        }

        String str = String.valueOf(val);

        Matcher matcher;
        if (val > 0.0 && (matcher = legacyVersionPattern.matcher(str)).matches()) {
            return Version.forIntegers(
                    pi(matcher.group(1)),
                    pi(matcher.group(2) + (ne(matcher.group(3)) ? matcher.group(3) : "0")),
                    ne(matcher.group(4)) ? pi(matcher.group(4)) : 0
            );
        }

        return super.deserialize(json, typeOfT, context);
    }

    private static int pi(String v) {
        return Integer.parseInt(v);
    }

    private static boolean ne(String v) {
        return StringUtils.isNotBlank(v);
    }
}
