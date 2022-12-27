package it.govhub.govregistry.readops.api.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.commons.api.beans.User;
import it.govhub.govregistry.commons.api.beans.UserList;
import it.govhub.govregistry.commons.api.beans.UserOrdering;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.UserMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.UserAssembler;
import it.govhub.govregistry.readops.api.repository.UserFilters;
import it.govhub.govregistry.readops.api.repository.UserRepository;
import it.govhub.govregistry.readops.api.spec.UserApi;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;


@RestController
public class ReadUserController implements UserApi{
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserAssembler userAssembler;
	
	@Autowired
	private SecurityService authService;
	
	
	@Override
	public ResponseEntity<UserList> listUsers(UserOrdering orderBy, Direction sortDirection, Integer limit, Long offset, String q, Boolean enabled) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_EDITOR, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_VIEWER);
		
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
	public ResponseEntity<User> readUser(Long id) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_EDITOR, GovregistryRoles.RUOLO_GOVREGISTRY_USERS_VIEWER);
		
		UserEntity user = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(UserMessages.notFound(id)));
		
		return ResponseEntity.ok(
				this.userAssembler.toModel(user));
	}

}
