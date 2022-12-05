package it.govhub.govregistry.api.exception;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

@Component
public class DefaultResponseErrorHandler extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
    	
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        
        errorAttributes.remove("timestamp");
        errorAttributes.remove("error");
        errorAttributes.remove("path");
        
        int status = (Integer) errorAttributes.get("status");
        
        if (status == 500) {
	        errorAttributes.put("title", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
	        errorAttributes.put("type", RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.INTERNAL_SERVER_ERROR));
	        errorAttributes.put("detail", "Request can't be satisfied at the moment");
        } else if (status == 404) {
        	errorAttributes.put("title", HttpStatus.NOT_FOUND.getReasonPhrase());
	        errorAttributes.put("type", RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.NOT_FOUND));
	        errorAttributes.put("detail", "Resource not found.");
        }
        
        return errorAttributes;
    }
}

