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

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.security.web.firewall.RequestRejectedException;

public class Base64String {

	private String value;
	
	private byte[] decodedValue;

	public Base64String(String value) {
		
		try {
			this.decodedValue = Base64.decodeBase64(value);
		} catch (Exception e) {
			throw new RequestRejectedException("Not a valid Base64: " + e.getMessage());
		}
		
		this.value = value;
	}

	public Base64String(byte[] value) {
		this.value = new String(Base64.encodeBase64(value));
	}

	public String getValue() {
		return value;
	}

	public byte[] getDecodedValue() {
		return decodedValue;
	}
}
