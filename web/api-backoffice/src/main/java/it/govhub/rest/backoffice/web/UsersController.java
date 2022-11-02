package it.govhub.rest.backoffice.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;

import it.govhub.management_bo.api.UsersApi;
import it.govhub.management_bo.beans.PatchOp;
import it.govhub.management_bo.beans.User;
import it.govhub.management_bo.beans.UserCreate;
import it.govhub.management_bo.beans.UserOrdering;

public class UsersController implements UsersApi {


	@Override
	public ResponseEntity<User> createUser(UserCreate userCreate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<User> readUser(UUID id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<Object> listUsers(Integer limit,	Long offset,	String q, Boolean enabled, UserOrdering orderBy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<User> updateUser(List<PatchOp> patchOp) {
		// TODO Auto-generated method stub
		return null;
	}



}
