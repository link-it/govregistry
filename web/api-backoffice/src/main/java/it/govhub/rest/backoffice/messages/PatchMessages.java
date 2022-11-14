package it.govhub.rest.backoffice.messages;

import org.springframework.validation.Errors;

import it.govhub.rest.backoffice.utils.RequestUtils;

public class PatchMessages {
	
	private PatchMessages() {}

	public static String voidObjectPatch = "Invalid PATCH operations: result is an empty object.";
	
	public static String validationFailed(Errors errors) {
		return "Patched object violates schema: " + RequestUtils.extractValidationError(errors.getAllErrors().get(0));		
	}
}
