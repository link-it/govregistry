package it.govhub.govregistry.api.assemblers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.api.beans.Authorization;
import it.govhub.govregistry.api.beans.OrganizationAuthItem;
import it.govhub.govregistry.api.beans.Role;
import it.govhub.govregistry.api.beans.ServiceAuthItem;
import it.govhub.govregistry.api.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.api.entity.RoleEntity;
import it.govhub.govregistry.api.spec.AuthorizationApi;

@Component
public class AuthorizationAssembler  extends RepresentationModelAssemblerSupport<RoleAuthorizationEntity, Authorization> {
	
	@Autowired
	private OrganizationAuthItemAssembler orgAuthItemAssembler;
	
	@Autowired
	private ServiceAuthItemAssembler serviceAuthItemAssembler;

	public AuthorizationAssembler() {
		super(AuthorizationApi.class, Authorization.class);
	}
	
	
	@Override	
	public Authorization toModel(RoleAuthorizationEntity auth) {
		Authorization ret = instantiateModel(auth);
		
		ret.setId(auth.getId());
		ret.setExpirationDate(auth.getExpirationDate());
		ret.setRole(this.toModel(auth.getRole()));
		
		List<ServiceAuthItem> services = auth.getServices().stream()
				.map(this.serviceAuthItemAssembler::toModel)
				.collect(Collectors.toList());
		
		ret.setServices(services);
		
		List<OrganizationAuthItem> organizations = auth.getOrganizations().stream()
				.map(this.orgAuthItemAssembler::toModel)
				.collect(Collectors.toList());
		
		ret.setOrganizations(organizations);
		
		return ret;
	}
	

	// I Ruoli non hanno link hateoas
	public Role toModel(RoleEntity role) {
		Role ret = new Role();
		BeanUtils.copyProperties(role, ret);
		ret.setRoleName(role.getName());
		ret.setAssignableRoles(
				role.getAssignableRoles().stream()
					.map(RoleEntity::getName)
					.collect(Collectors.toList())
				);
		return ret;
	}
	
	
}
