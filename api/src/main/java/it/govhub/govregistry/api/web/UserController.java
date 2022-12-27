package it.govhub.govregistry.api.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.govregistry.api.services.UserService;
import it.govhub.govregistry.api.spec.UserApi;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.api.beans.User;
import it.govhub.govregistry.commons.api.beans.UserCreate;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.govregistry.readops.api.assemblers.UserAssembler;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;


@RestController
public class UserController implements UserApi {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserAssembler userAssembler;
	
	@Autowired
	private SecurityService authService;
	
	
	@Override
	public ResponseEntity<User> updateUser(Long id, List<PatchOp> patchOp) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_EDITOR);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		UserEntity newUser = this.userService.patchUser(id, patch);

		// Dalla entity ripasso al model
		User ret = this.userAssembler.toModel(newUser);
		
		return ResponseEntity.ok(ret);
	}
	
	
	@Override
	public ResponseEntity<User> createUser(UserCreate userCreate) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_EDITOR);
		
		UserEntity newUser = this.userService.createUser(userCreate);

		User ret = this.userAssembler.toModel(newUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

}
