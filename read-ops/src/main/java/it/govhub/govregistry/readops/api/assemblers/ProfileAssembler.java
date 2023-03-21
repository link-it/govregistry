package it.govhub.govregistry.readops.api.assemblers;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.web.ProfileController;

@Component
public class ProfileAssembler extends RepresentationModelAssemblerSupport<UserEntity, Profile> {
	
	@Autowired
	AuthorizationConverter authAssembler;
	
	Logger log = LoggerFactory.getLogger(ProfileAssembler.class);

	public ProfileAssembler() {
		super(ProfileController.class, Profile.class);
	}

	@Override
	public Profile toModel(UserEntity src) {
		log.debug("Assembling Entity [User] to model...");
		Profile ret = new Profile();
		
		BeanUtils.copyProperties(src, ret);
		
		List<Authorization> auths = src.getAuthorizations().stream()
			.map(this.authAssembler::toModel)
			.collect(Collectors.toList());
		
		ret.setAuthorizations(auths);
		
		return ret;
	}
	
	public Profile toModel(UserEntity src, String applicationId) {
		log.debug("Assembling Entity [User] to model, filtering out authorizations by applicationId...");
		
		Profile ret = new Profile();
		
		BeanUtils.copyProperties(src, ret);
		
		List<Authorization> auths = src.getAuthorizations().stream()
			.filter( auth -> auth.getRole().getGovhubApplication().getApplicationId().equals(applicationId))	
			.map(this.authAssembler::toModel)
			.collect(Collectors.toList());
		
		ret.setAuthorizations(auths);
		
		return ret;
	}

}
