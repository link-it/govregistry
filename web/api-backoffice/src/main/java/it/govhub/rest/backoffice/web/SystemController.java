package it.govhub.rest.backoffice.web;

import org.springframework.http.ResponseEntity;

import it.govhub.management_bo.api.StatusApi;
import it.govhub.management_bo.beans.Problem;

public class SystemController implements StatusApi {

	@Override
	public ResponseEntity<Problem> status() {
		// TODO Auto-generated method stub
		return null;
	}


}
