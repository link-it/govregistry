package it.govhub.govregistry.readops.api.assemblers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Authorization;
import it.govhub.govregistry.commons.api.beans.Profile;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.spec.ProfileApi;

@Component
public class ProfileAssembler extends RepresentationModelAssemblerSupport<UserEntity, Profile> {
	
	@Autowired
	AuthorizationConverter authAssembler;

	public ProfileAssembler() {
		super(ProfileApi.class, Profile.class);
	}

	@Override
	public Profile toModel(UserEntity src) {
		Profile ret = new Profile();
		
		BeanUtils.copyProperties(src, ret);
		
		List<Authorization> auths = src.getAuthorizations().stream()
			.map(this.authAssembler::toModel)
			.collect(Collectors.toList());
		
		ret.setAuthorizations(auths);
		
		return ret;
	}
	
	public Profile toModel(UserEntity src, String applicationId) {
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
