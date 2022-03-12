package ru.turikhay.util.json;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

public final class ExposeExclusion implements ExclusionStrategy {
    private static ExposeExclusion instance;

    private static ExposeExclusion getInstance() {
        if (instance == null) {
            instance = new ExposeExclusion();
        }
        return instance;
    }

    public static GsonBuilder setup(GsonBuilder b) {
        b.addSerializationExclusionStrategy(getInstance());
        b.addDeserializationExclusionStrategy(getInstance());
        return b;
    }

    private ExposeExclusion() {
    }

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        final Expose expose = fieldAttributes.getAnnotation(Expose.class);
        return expose != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
