package it.govhub.govregistry.commons.utils;

import java.nio.file.Path;

import javax.persistence.AttributeConverter;

public class JpaPathConverter implements AttributeConverter<Path, String> {

	@Override
	public String convertToDatabaseColumn(Path value) {
		return value.toString();
	}

	@Override
	public Path convertToEntityAttribute(String dbData) {
		return Path.of(dbData);
	}

}
