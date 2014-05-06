package net.minecraft.launcher.versions.json;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.turikhay.exceptions.ParseException;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateTypeAdapter implements JsonDeserializer<Date>,
		JsonSerializer<Date> {
	private final DateFormat enUsFormat;
	private final DateFormat iso8601Format;

	public DateTypeAdapter() {
		this.enUsFormat = DateFormat.getDateTimeInstance(2, 2, Locale.US);
		this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	}

	@Override
	public Date deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonPrimitive))
			throw new JsonParseException("The date should be a string value");

		Date date = toDate(json.getAsString());

		if (typeOfT == Date.class)
			return date;

		throw new IllegalArgumentException(getClass()
				+ " cannot deserialize to " + typeOfT);
	}

	@Override
	public JsonElement serialize(Date src, Type typeOfSrc,
			JsonSerializationContext context) {
		synchronized (this.enUsFormat) {
			return new JsonPrimitive(toString(src));
		}
	}

	public String toString(Date date) {
		synchronized (this.enUsFormat) {
			String result = this.iso8601Format.format(date);
			return result.substring(0, 22) + ":" + result.substring(22);
		}
	}
	
	public Date toDate(String string) {
		synchronized (this.enUsFormat) {
			try {
				return this.enUsFormat.parse(string);
			} catch (Exception ignored) {
			}

			try {
				return this.iso8601Format.parse(string);
			} catch (Exception ignored) {
			}

			try {
				String cleaned = string.replace("Z", "+00:00");
				cleaned = cleaned.substring(0, 22) + cleaned.substring(23);
				return this.iso8601Format.parse(cleaned);
			} catch (Exception e) {
				throw new ParseException("Invalid date: "
						+ string, e);
			}
		}
	}
}
