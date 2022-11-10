package it.govhub.rest.backoffice;

import org.apache.commons.codec.binary.Base64;
import org.springframework.security.web.firewall.RequestRejectedException;

public class Base64String {

	public String value;

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
		
	}
}
