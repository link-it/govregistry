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

