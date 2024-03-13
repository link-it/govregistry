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
		}
		return patch;
	}

	
	public static String extractValidationError(ObjectError error) {
		if (error instanceof FieldError) {			
			var ferror = (FieldError) error;
			
			return "Field error in object '" + error.getObjectName() + "' on field '" + ferror.getField() +
					"': rejected value [" + ObjectUtils.nullSafeToString(ferror.getRejectedValue()) + "]; " +
					error.getDefaultMessage();
		}
		return error.toString();
	}

	
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
