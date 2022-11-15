package it.govhub.rest.backoffice.assemblers;

import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.rest.backoffice.beans.Authorization;
import it.govhub.rest.backoffice.beans.Role;
import it.govhub.rest.backoffice.entity.RoleAuthorizationEntity;
import it.govhub.rest.backoffice.entity.RoleEntity;
import it.govhub.rest.backoffice.web.AuthorizationController;

@Component
public class AuthorizationAssembler  extends RepresentationModelAssemblerSupport<RoleAuthorizationEntity, Authorization> {
	
	@Autowired
	private OrganizationAuthItemAssembler orgAuthItemAssembler;

	public AuthorizationAssembler() {
		super(AuthorizationController.class, Authorization.class);
	}
	
	
	@Override	
	public Authorization toModel(RoleAuthorizationEntity auth) {
		Authorization ret = instantiateModel(auth);
		
		ret.setId(auth.getId());
		ret.setExpirationDate(auth.getExpirationDate());
		ret.setRole(this.toModel(auth.getRole()));
		ret.setServices(Collections.emptyList());	// TODO
		
		var  organizations = auth.getOrganizations().stream()
				.map(this.orgAuthItemAssembler::toModel)
				.collect(Collectors.toList());
		
		ret.setOrganizations(organizations);
		
		return ret;
	}
	

	// I Ruoli non hanno link hateoas
	public Role toModel(RoleEntity role) {
		Role ret = new Role();
		BeanUtils.copyProperties(role, ret);
		return ret;
	}
	
	
}
