package ru.turikhay.tlauncher.bootstrap.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

final class ExposeExclusion {
    private static final Excluder serialization = new SerializationExcluder(), deserialization = new DeserializationExcluder();

    static GsonBuilder setup(GsonBuilder b) {
        b.addSerializationExclusionStrategy(serialization);
        b.addDeserializationExclusionStrategy(deserialization);
        return b;
    }

    private static abstract class Excluder implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            final Expose expose = fieldAttributes.getAnnotation(Expose.class);
            return expose != null && checkAnnotation(expose);
        }

        abstract boolean checkAnnotation(Expose annotation);

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    }

    private static class SerializationExcluder extends Excluder {
        @Override
        boolean checkAnnotation(Expose annotation) {
            return annotation.serialize();
        }
    }

    private static class DeserializationExcluder extends Excluder {
        @Override
        boolean checkAnnotation(Expose annotation) {
            return annotation.deserialize();
        }
    }
}
