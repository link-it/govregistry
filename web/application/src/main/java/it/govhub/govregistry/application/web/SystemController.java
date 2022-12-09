package it.govhub.govregistry.application.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.govregistry.api.beans.Problem;
import it.govhub.govregistry.api.exception.RestResponseEntityExceptionHandler;
import it.govhub.govregistry.api.spec.SystemApi;

@RestController
public class SystemController implements SystemApi {
	
	@Override
	public ResponseEntity<Problem> status() {
		Problem ret =  RestResponseEntityExceptionHandler.buildProblem(HttpStatus.OK,"System is working correctly");
		ret.removeLinks();
		
		return ResponseEntity.ok(ret);
	}

}
