package it.govhub.govregistry.application.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.govregistry.api.assemblers.UserAssembler;
import it.govhub.govregistry.api.beans.PatchOp;
import it.govhub.govregistry.api.beans.User;
import it.govhub.govregistry.api.beans.UserCreate;
import it.govhub.govregistry.api.beans.UserList;
import it.govhub.govregistry.api.beans.UserOrdering;
import it.govhub.govregistry.api.config.SecurityConstants;
import it.govhub.govregistry.api.entity.UserEntity;
import it.govhub.govregistry.api.exception.ResourceNotFoundException;
import it.govhub.govregistry.api.messages.UserMessages;
import it.govhub.govregistry.api.repository.UserFilters;
import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.api.services.SecurityService;
import it.govhub.govregistry.api.services.UserService;
import it.govhub.govregistry.api.spec.UserApi;
import it.govhub.govregistry.api.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.api.utils.ListaUtils;
import it.govhub.govregistry.api.utils.RequestUtils;


@RestController
public class UserController implements UserApi {
	

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserAssembler userAssembler;
	
	@Autowired
	private SecurityService authService;
	
	@Override
	public ResponseEntity<User> readUser(Long id) {
		
		this.authService.hasAnyRole(SecurityConstants.RUOLO_GOVHUB_SYSADMIN, SecurityConstants.RUOLO_GOVHUB_USERS_EDITOR, SecurityConstants.RUOLO_GOVHUB_USERS_VIEWER);
		
		UserEntity user = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(UserMessages.notFound(id)));
		
		return ResponseEntity.ok(
				this.userAssembler.toModel(user));
	}

	
	@Override
	public ResponseEntity<UserList> listUsers(UserOrdering orderBy, Direction sortDirection, Integer limit, Long offset, String q, Boolean enabled) {
		
		this.authService.hasAnyRole(SecurityConstants.RUOLO_GOVHUB_SYSADMIN, SecurityConstants.RUOLO_GOVHUB_USERS_EDITOR, SecurityConstants.RUOLO_GOVHUB_USERS_VIEWER);
		
		Specification<UserEntity> spec = UserFilters.empty();
		if (q != null) {
			spec = UserFilters.likeFullName(q).or(UserFilters.likePrincipal(q));
		}
		if (enabled != null) {
			spec = spec.and(UserFilters.byEnabled(enabled));
		}
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, UserFilters.sort(orderBy,sortDirection));
		
		Page<UserEntity> users = this.userRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		UserList ret = ListaUtils.costruisciListaPaginata(users, pageRequest.limit, curRequest, new UserList());
		
		for (UserEntity user : users) {
			ret.addItemsItem(this.userAssembler.toModel(user));
		}
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<User> updateUser(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyRole(SecurityConstants.RUOLO_GOVHUB_SYSADMIN, SecurityConstants.RUOLO_GOVHUB_USERS_EDITOR);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		UserEntity newUser = this.userService.patchUser(id, patch);

		// Dalla entity ripasso al model
		User ret = this.userAssembler.toModel(newUser);
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<User> createUser(UserCreate userCreate) {
		
		this.authService.hasAnyRole(SecurityConstants.RUOLO_GOVHUB_SYSADMIN, SecurityConstants.RUOLO_GOVHUB_USERS_EDITOR);
		
		UserEntity newUser = this.userService.createUser(userCreate);

		User ret = this.userAssembler.toModel(newUser);
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

}