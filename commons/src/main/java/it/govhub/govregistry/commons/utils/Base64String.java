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

	/* 
	 * Costruisce un'istanza di questa classe codificando in base64 un array di bytes.
	 *  
	 */
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
