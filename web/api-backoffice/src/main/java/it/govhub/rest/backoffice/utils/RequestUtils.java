package it.govhub.rest.backoffice.utils;

import java.io.IOException;
import java.util.List;

import org.springframework.util.ObjectUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.rest.backoffice.beans.PatchOp;

public class RequestUtils {

	
	
	public static JsonPatch toJsonPatch(List<PatchOp> patchOp) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyPatch = mapper.valueToTree(patchOp);
		
		JsonPatch patch;
		try {
			patch = JsonPatch.fromJson(bodyPatch);
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return patch;
	}

	
	/**
	 * Crea un messaggio che descrive un errore di validazione è più leggibile 
	 * per una API rispetto a quello restituito di default.
	 * 	
	 */
	public static String extractValidationError(ObjectError error) {
		if (error instanceof FieldError) {			
			var ferror = (FieldError) error;
			
			return "Field error in object '" + error.getObjectName() + "' on field '" + ferror.getField() +
					"': rejected value [" + ObjectUtils.nullSafeToString(ferror.getRejectedValue()) + "]; " +
					error.getDefaultMessage();
		}
		return error.toString();
	}
}
