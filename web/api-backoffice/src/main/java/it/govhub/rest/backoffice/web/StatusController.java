package it.govhub.rest.backoffice.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.rest.backoffice.api.StatusApi;
import it.govhub.rest.backoffice.beans.Problem;
import it.govhub.rest.backoffice.exception.RestResponseEntityExceptionHandler;

@RestController
public class StatusController implements StatusApi {

	@Override
	public ResponseEntity<Problem> status() {
		
		Problem ret =  RestResponseEntityExceptionHandler.buildProblem(HttpStatus.OK,"System is working correctly");
		ret.removeLinks();
		
		return ResponseEntity.ok(ret);
	}

}
