package it.govhub.govregistry.api.messages;

import org.springframework.validation.Errors;

import it.govhub.govregistry.api.utils.RequestUtils;

public class PatchMessages {
	
	private PatchMessages() {}

	public static final String VOID_OBJECT_PATCH = "Invalid PATCH operations: result is an empty object.";
	
	public static String validationFailed(Errors errors) {
		return "Patched object violates schema: " + RequestUtils.extractValidationError(errors.getAllErrors().get(0));		
	}
}