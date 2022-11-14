package it.govhub.rest.backoffice.utils;

import it.govhub.rest.backoffice.exception.BadRequestException;

public class PostgreSQLUtilities {
	
	private PostgreSQLUtilities() {}

	public static final String NULL_BYTE_SEQUENCE = "\u0000";
	
	public static boolean isInvalid0x00error(Throwable t) {
		return t.getMessage()!=null && t.getMessage().contains("invalid byte sequence for encoding \"UTF8\": 0x00");
	}
	
	public static boolean containsNullByteSequence(String s) {
		return s!=null && s.contains(NULL_BYTE_SEQUENCE);
	}
	
	public static void throwIfContainsNullByte(String s, String field) {
		if (s != null && containsNullByteSequence(s)) {
				throw new BadRequestException(field + ": Sequenza di byte non vaida per encoding \"UTF8\": 0x00");
		}
	}
	
	public static String normalizeString(String s) {
		
		// invalid byte sequence for encoding "UTF8": 0x00
		// PostgreSQL non supporta il salvataggio di NULL (\0x00) caratteri nei campi text. 
		return s.replace(NULL_BYTE_SEQUENCE, "");

	}
	
}