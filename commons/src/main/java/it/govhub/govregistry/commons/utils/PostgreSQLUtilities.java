/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govregistry.commons.utils;

import it.govhub.govregistry.commons.exception.BadRequestException;

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