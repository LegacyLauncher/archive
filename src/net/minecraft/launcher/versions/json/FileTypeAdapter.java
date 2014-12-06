package net.minecraft.launcher.versions.json;

import java.io.File;
import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class FileTypeAdapter extends TypeAdapter<File> {
	@Override
	public void write(JsonWriter out, File value) throws IOException {
		if (value == null)
			out.nullValue();
		else
			out.value(value.getAbsolutePath());
	}

	@Override
	public File read(JsonReader in) throws IOException {
		if (in.hasNext()) {
			String name = in.nextString();
			return name != null ? new File(name) : null;
		}
		return null;
	}
}
