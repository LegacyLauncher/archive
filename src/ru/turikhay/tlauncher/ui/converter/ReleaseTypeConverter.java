package ru.turikhay.tlauncher.ui.converter;

import net.minecraft.launcher.versions.ReleaseType;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class ReleaseTypeConverter extends LocalizableStringConverter<ReleaseType> {

	public ReleaseTypeConverter() {
		super("version.description");
	}

	@Override
	public ReleaseType fromString(String from) {
		if(from == null)
			return ReleaseType.UNKNOWN;
		
		for(ReleaseType type : ReleaseType.values())
			if(type.toString().equals(from))
				return type;
		return null;
	}

	@Override
	public String toValue(ReleaseType from) {
		if(from == null)
			return ReleaseType.UNKNOWN.toString();
		
		return from.toString();
	}

	@Override
	protected String toPath(ReleaseType from) {
		if(from == null)
			return ReleaseType.UNKNOWN.toString();
		
		return toValue(from);
	}

	@Override
	public Class<ReleaseType> getObjectClass() {
		return ReleaseType.class;
	}

}
