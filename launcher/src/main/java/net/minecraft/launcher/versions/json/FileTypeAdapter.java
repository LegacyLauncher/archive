package net.minecraft.launcher.versions.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.File;
import java.io.IOException;

public class FileTypeAdapter extends TypeAdapter<File> {
    public void write(JsonWriter out, File value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.getAbsolutePath());
        }

    }

    public File read(JsonReader in) throws IOException {
        if (in.hasNext()) {
            String name = in.nextString();
            return name != null ? new File(name) : null;
        } else {
            return null;
        }
    }
}
