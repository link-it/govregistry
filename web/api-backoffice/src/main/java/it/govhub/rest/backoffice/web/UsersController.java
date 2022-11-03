package it.govhub.rest.backoffice.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.management_bo.api.UsersApi;
import it.govhub.management_bo.beans.PatchOp;
import it.govhub.management_bo.beans.User;
import it.govhub.management_bo.beans.UserCreate;
import it.govhub.management_bo.beans.UserList;
import it.govhub.management_bo.beans.UserOrdering;

@RestController
public class UsersController implements UsersApi {

	@Override
	public ResponseEntity<User> createUser(UserCreate userCreate) {
		return ResponseEntity.ok(new User());
	}

	@Override
	public ResponseEntity<User> readUser(UUID id) {
		return ResponseEntity.ok(new User());
	}

	@Override
	public ResponseEntity<UserList> listUsers(Integer limit, Long offset, String q, Boolean enabled, UserOrdering orderBy) {
		return ResponseEntity.ok(new UserList());
	}

	@Override
	public ResponseEntity<User> updateUser(List<PatchOp> patchOp) {
		return ResponseEntity.ok(new User());
	}

}
