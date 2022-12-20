package it.govhub.govregistry.commons.utils;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.web.firewall.RequestRejectedException;

public class Base64String {

	private String value;

	public Base64String(String value) {
		
		if  (value.getBytes().length < 2 || !Base64.isBase64(value.getBytes())) {
			throw new RequestRejectedException("Not a valid Base64");
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
}
