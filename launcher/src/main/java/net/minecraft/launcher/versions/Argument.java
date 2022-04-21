package net.minecraft.launcher.versions;

import com.google.gson.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Argument {
    private final String[] value;
    private final List<Rule> compatibilityRules;

    public Argument(String[] values, List<Rule> Rules) {
        this.value = values;
        this.compatibilityRules = Rules;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("value", value)
                .append("compatibilityRules", compatibilityRules)
                .build();
    }

    public List<String> apply(Rule.FeatureMatcher featureMatcher, StrSubstitutor substitutor) {
        ArrayList<String> output = new ArrayList<>();
        if (appliesToCurrentEnvironment(featureMatcher)) {
            for (String value : this.value) {
                output.add(substitutor == null ? value : substitutor.replace(value));
            }
        }
        return output;
    }

    public boolean appliesToCurrentEnvironment(Rule.FeatureMatcher featureMatcher) {
        if (this.compatibilityRules == null) return true;
        Rule.Action lastAction = Rule.Action.DISALLOW;

        for (Rule Rule : this.compatibilityRules) {
            Rule.Action action = Rule.getAppliedAction(featureMatcher);
            if (action != null) {
                lastAction = action;
            }
        }
        return lastAction == Rule.Action.ALLOW;
    }

    public static class Serializer implements JsonDeserializer<Argument>, JsonSerializer<Argument> {
        public Argument deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return new Argument(new String[]{json.getAsString()}, null);
            }
            if (json.isJsonObject()) {
                JsonObject obj = json.getAsJsonObject();
                JsonElement rawValues;
                if (obj.has("values")) {
                    rawValues = obj.get("values");
                } else {
                    rawValues = obj.get("value");
                }
                String[] values;
                if (rawValues.isJsonPrimitive()) {
                    values = new String[]{rawValues.getAsString()};
                } else {
                    JsonArray array = rawValues.getAsJsonArray();
                    values = new String[array.size()];
                    for (int i = 0; i < array.size(); i++) {
                        values[i] = array.get(i).getAsString();
                    }
                }
                List<Rule> rules = null;
                if (obj.has("rules")) {
                    rules = new ArrayList<>();
                    JsonArray array = obj.getAsJsonArray("rules");
                    for (JsonElement element : array) {
                        rules.add(context.deserialize(element, Rule.class));
                    }
                }
                return new Argument(values, rules);
            }
            throw new JsonParseException("Invalid argument, must be object or string");
        }

        @Override
        public JsonElement serialize(Argument src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            if (src.value.length == 1) {
                object.addProperty("value", src.value[0]);
            } else {
                JsonArray array = new JsonArray();
                for (String v : src.value) {
                    array.add(v);
                }
                object.add("value", array);
            }
            if (src.compatibilityRules != null && !src.compatibilityRules.isEmpty()) {
                JsonArray array = new JsonArray();
                for (Rule rule : src.compatibilityRules) {
                    array.add(context.serialize(rule));
                }
                object.add("rules", array);
            }
            return object;
        }
    }
}
