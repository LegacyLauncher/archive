package ru.turikhay.tlauncher.minecraft.auth;

import java.io.IOException;
import java.util.UUID;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class UUIDTypeAdapter extends TypeAdapter<UUID>
{
	public void write(JsonWriter out, UUID value) throws IOException {
		out.value(fromUUID(value));
	}

	public UUID read(JsonReader in) throws IOException {
		return fromString(in.nextString());
	}
	
	public static String toUUID(String value) {
		if(value == null) return null;
		return value.replace("-", "");
	}
	
	public static String fromUUID(UUID value) {
		return toUUID(value.toString());
	}

	public static UUID fromString(String input) {
		return UUID.fromString(input.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
	}
}
