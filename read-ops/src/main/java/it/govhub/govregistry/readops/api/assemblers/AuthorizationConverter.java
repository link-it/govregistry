package it.govhub.govregistry.readops.api.assemblers;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.OrganizationAuthItem;
import it.govhub.govregistry.commons.api.beans.Role;
import it.govhub.govregistry.commons.api.beans.ServiceAuthItem;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.entity.RoleEntity;

@Component
public class AuthorizationConverter {
	
	@Autowired
	OrganizationAuthItemAssembler orgAuthItemAssembler;
	
	@Autowired
	ServiceAuthItemAssembler serviceAuthItemAssembler;
	
	Logger log = LoggerFactory.getLogger(AuthorizationConverter.class);

	
	public Authorization toModel(RoleAuthorizationEntity auth) {
		log.debug("Assembling Entity [RoleAuthorization] to model.");  
		Authorization ret = new Authorization();
		
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
		log.debug("Assembling Entity [Role] to model...");
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
