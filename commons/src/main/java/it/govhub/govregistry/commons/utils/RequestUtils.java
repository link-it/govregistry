package it.govhub.govregistry.commons.utils;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.exception.UnreachableException;

public class RequestUtils {
	
	static Logger log = LoggerFactory.getLogger(RequestUtils.class);
	
	private RequestUtils() {}
	
	public static JsonPatch toJsonPatch(List<PatchOp> patchOp) {
		log.debug("Building the JsonPatch object...");
		ObjectMapper mapper = new ObjectMapper();
		JsonNode bodyPatch = mapper.valueToTree(patchOp);
		
		JsonPatch patch;
		try {
			patch = JsonPatch.fromJson(bodyPatch);
		} 
		catch (IOException e) {
			throw new UnreachableException("Body of List<PatchOp> should always be convertible to JsonPatch");
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
