/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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
package it.govhub.govregistry.commons.exception.handlers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import it.govhub.govregistry.commons.messages.SystemMessages;

/**
 * Handler di fallback quando il @ControllerAdvice non gestisce l'errore sollevato durante la richiesta.
 * Trasforma gli errorAttributes in modo che venga prodotto un Problem secondo specifica.
 *
 */
@Component
public class DefaultResponseErrorHandler extends DefaultErrorAttributes {
	
	Logger logger = LoggerFactory.getLogger(DefaultResponseErrorHandler.class);

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
    	logger.debug("Running default Response Error Handler.");
    	
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        
        errorAttributes.remove("timestamp");
        errorAttributes.remove("error");
        errorAttributes.remove("path");
        
        int status = (Integer) errorAttributes.get("status");
        
        if (status == 500) {
	        errorAttributes.put("title", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
	        errorAttributes.put("type", RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.INTERNAL_SERVER_ERROR));
	        errorAttributes.put("detail", SystemMessages.internalError());
        } else if (status == 404) {
        	errorAttributes.put("title", HttpStatus.NOT_FOUND.getReasonPhrase());
	        errorAttributes.put("type", RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.NOT_FOUND));
	        errorAttributes.put("detail", SystemMessages.endpointNotFound());
        }
        
        return errorAttributes;
    }
}

