package it.govhub.govregistry.api.assemblers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

import it.govhub.commons.profile.api.beans.Authorization;
import it.govhub.commons.profile.api.beans.Profile;
import it.govhub.commons.profile.api.spec.ProfileApi;
import it.govhub.govregistry.commons.entity.UserEntity;

public class ProfileAssembler extends RepresentationModelAssemblerSupport<UserEntity, Profile> {
	
	@Autowired
	AuthorizationAssembler authAssembler;

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

}
