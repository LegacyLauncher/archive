package ru.turikhay.tlauncher.minecraft.crash;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import ru.turikhay.tlauncher.TLauncher;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class CrashSignatureContainer {
	private final static int universalExitCode = CrashDescriptor.goodExitCode;

	private Map<String, String> variables = new LinkedHashMap<String, String>();
	private List<CrashSignature> signatures = new ArrayList<CrashSignature>();

	public Map<String, String> getVariables() {
		return variables;
	}

	public List<CrashSignature> getSignatures() {
		return signatures;
	}

	public String getVariable(String key) {
		return variables.get(key);
	}

	public Pattern getPattern(String key) {
		return Pattern.compile(variables.get(key));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{\nvariables='" + variables
				+ "',\nsignatures='" + signatures + "'}";
	}

	public class CrashSignature {
		private String name, version, path, pattern;
		private int exit;
		private boolean fake, forge;

		private Pattern versionPattern;
		private Pattern linePattern;

		public String getName() {
			return name;
		}

		public Pattern getVersion() {
			return versionPattern;
		}

		public boolean hasVersion() {
			return version != null;
		}

		public boolean isFake() {
			return fake;
		}

		public Pattern getPattern() {
			return linePattern;
		}

		public boolean hasPattern() {
			return pattern != null;
		}

		public String getPath() {
			return path;
		}

		public int getExitCode() {
			return exit;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "{name='" + name
					+ "', version='" + version + "', path='" + path
					+ "', pattern='" + pattern + "', exitCode=" + exit
					+ ", forge=" + forge + ", versionPattern='"
					+ versionPattern + "', linePattern='" + linePattern + "'}";
		}
	}

	private static class CrashSignatureListSimpleDeserializer {
		private final Gson defaultContext;

		private Map<String, String> variables;
		private String forgePrefix;

		CrashSignatureListSimpleDeserializer() {
			this.defaultContext = TLauncher.getGson();
		}

		public void setVariables(Map<String, String> vars) {
			this.variables = (vars == null) ? new HashMap<String, String>()
					: vars;
			this.forgePrefix = (variables.containsKey("forge")) ? variables
					.get("forge") : "";
		}

		public List<CrashSignature> deserialize(JsonElement elem)
				throws JsonParseException {
			List<CrashSignature> signatureList = defaultContext.fromJson(elem,
					new TypeToken<List<CrashSignature>>() {
			}.getType());

			for (CrashSignature signature : signatureList)
				this.analyzeSignature(signature);

			return signatureList;
		}

		private CrashSignature analyzeSignature(CrashSignature signature) {
			if (signature.name == null || signature.name.isEmpty())
				throw new JsonParseException("Invalid name: \""
						+ signature.name + "\"");

			if (signature.version != null) {
				String pattern = signature.version;

				for (Entry<String, String> en : variables.entrySet()) {
					String varName = en.getKey(), varVal = en.getValue();
					pattern = pattern.replace("${" + varName + "}", varVal);
				}

				signature.versionPattern = Pattern.compile(pattern);
			}

			if (signature.pattern != null) {
				String pattern = signature.pattern;

				for (Entry<String, String> en : variables.entrySet()) {
					String varName = en.getKey(), varVal = en.getValue();
					pattern = pattern.replace("${" + varName + "}", varVal);
				}

				if (signature.forge)
					pattern = forgePrefix + pattern;

				signature.linePattern = Pattern.compile(pattern);
			}

			if (signature.versionPattern == null
					&& signature.linePattern == null
					&& signature.exit == universalExitCode)
				throw new JsonParseException("Useless signature found: "
						+ signature.name);

			return signature;
		}
	}

	static class CrashSignatureContainerDeserializer implements
	JsonDeserializer<CrashSignatureContainer> {
		private final CrashSignatureListSimpleDeserializer listDeserializer;
		private final Gson defaultContext;

		CrashSignatureContainerDeserializer() {
			this.defaultContext = TLauncher.getGson();
			this.listDeserializer = new CrashSignatureListSimpleDeserializer();
		}

		@Override
		public CrashSignatureContainer deserialize(JsonElement element,
				Type type, JsonDeserializationContext context)
						throws JsonParseException {
			JsonObject object = element.getAsJsonObject();

			Map<String, String> rawVariables = defaultContext.fromJson(
					object.get("variables"),
					new TypeToken<Map<String, String>>() {
					}.getType());
			Map<String, String> variables = new LinkedHashMap<String, String>();

			for (Entry<String, String> rawEn : rawVariables.entrySet()) {
				String varName = rawEn.getKey(), varVal = rawEn.getValue();

				for (Entry<String, String> en : variables.entrySet()) {
					String replaceName = en.getKey(), replaceVal = en
							.getValue();
					varVal = varVal.replace("${" + replaceName + "}",
							replaceVal);
				}

				variables.put(varName, varVal);
			}

			listDeserializer.setVariables(variables);

			List<CrashSignature> signatures = listDeserializer
					.deserialize(object.get("signatures"));

			CrashSignatureContainer list = new CrashSignatureContainer();
			list.variables = variables;
			list.signatures = signatures;

			return list;
		}
	}
}
