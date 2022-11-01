package it.govhub.rest.backoffice.web;

import org.springframework.http.ResponseEntity;

import it.govhub.management_bo.api.UsersApi;
import it.govhub.management_bo.beans.User;

public class UsersController implements UsersApi {

	@Override
	public ResponseEntity<User> createUser(User body) {
		// TODO Auto-generated method stub
		return null;
	}



}
