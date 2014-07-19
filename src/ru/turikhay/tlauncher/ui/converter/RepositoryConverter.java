package ru.turikhay.tlauncher.ui.converter;

import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class RepositoryConverter extends LocalizableStringConverter<Repository> {

	public RepositoryConverter() {
		super("repo");
	}

	@Override
	public Repository fromString(String from) {
		if(from == null)
			return null;
		
		for(Repository type : Repository.values())
			if(type.toString().equals(from))
				return type;
		
		return null;
	}

	@Override
	public String toValue(Repository from) {
		return from.toString();
	}

	@Override
	protected String toPath(Repository from) {
		return toValue(from);
	}

	@Override
	public Class<Repository> getObjectClass() {
		return Repository.class;
	}

}
