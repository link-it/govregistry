package it.govhub.rest.backoffice.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.govhub.rest.backoffice.api.UsersApi;
import it.govhub.rest.backoffice.assemblers.UserAssembler;
import it.govhub.rest.backoffice.beans.PatchOp;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.beans.UserList;
import it.govhub.rest.backoffice.beans.UserOrdering;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.ConflictException;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.repository.UserRepository;
import it.govhub.rest.backoffice.services.UserService;


@RestController
public class UsersController implements UsersApi {
	

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserAssembler userAssembler;

	@Override
	public ResponseEntity<User> readUser(Long id) {
		
		UserEntity user = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException("User with ID ["+id+"] not found"));
		
		User ret = this.userAssembler.toModel(user);
		
		return ResponseEntity.ok(ret);
	}

	@Override
	public ResponseEntity<UserList> listUsers(Integer limit, Long offset, String q, Boolean enabled, UserOrdering orderBy) {
		return ResponseEntity.ok(new UserList());
	}

	@Override
	public ResponseEntity<User> updateUser(List<PatchOp> patchOp) {
		return ResponseEntity.ok(new User());
	}

	@Override
	public ResponseEntity<User> createUser(@Valid UserCreate userCreate) {
		
		Optional<UserEntity> found = this.userRepo.findByPrincipal(userCreate.getPrincipal());
		if (found.isPresent()) {
			throw new ConflictException("User with principal ["+userCreate.getPrincipal()+"] already exists");
		}
		
		UserEntity newUser = UserEntity.builder()
				.principal(userCreate.getPrincipal())
				.full_name(userCreate.getFullName())
				.enabled(userCreate.isEnabled())
				.email(userCreate.getEmail())
				.build();
		
		newUser = this.userRepo.save(newUser);
		
		User ret = this.userAssembler.toModel(newUser);		
		
		return ResponseEntity.ok(ret);
	}

}
