package it.govhub.govregistry.commons.utils;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.exception.BadRequestException;
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
			throw new BadRequestException(e);
			//throw new UnreachableException("Body of List<PatchOp> should always be convertible to JsonPatch");
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

	
	/**
	 * Utilizzata nelle richieste multipart di upload file per estrarre il nome del file dallo header
	 * Content-Disposition
	 * 
	 */
	public static String readFilenameFromHeaders(FileItemHeaders headers) {
		
		String filename = null;
		try {
	    	String contentDisposition = headers.getHeader("Content-Disposition");
	    	log.debug("Content Disposition Header: {}", contentDisposition);
	    	
	    	String[] headerDirectives = contentDisposition.split(";");
	    	
	    	for(String directive : headerDirectives) {
	    		String[] keyValue = directive.split("=");
	    		if (StringUtils.equalsIgnoreCase(keyValue[0].trim(), "filename")) {
	    			// Rimuovo i doppi apici
	    			filename = keyValue[1].trim().substring(1, keyValue[1].length()-1);
	    		}
	    	}
		} catch (Exception e) {
			log.error("Exception while reading header: {}", e);
			filename = null;
		}
		
		return filename;
	}
	
}
