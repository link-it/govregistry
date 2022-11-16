package it.govhub.rest.backoffice.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.rest.backoffice.beans.Authorization;
import it.govhub.rest.backoffice.beans.Profile;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.web.UserController;

@Component
public class UserAssembler  extends RepresentationModelAssemblerSupport<UserEntity, User> {
	
	@Autowired
	AuthorizationAssembler authAssembler;

	public UserAssembler() {
		super(UserController.class, User.class);
	}

	@Override
	public User toModel(UserEntity src) {
		User ret = instantiateModel(src);
		
        BeanUtils.copyProperties(src, ret);

		ret.add(linkTo(
					methodOn(UserController.class)
					.readUser(src.getId()))
				.withSelfRel()
			) ;
				
		return ret;
	}
	

	public Profile toProfileModel(UserEntity src) {
		Profile ret = new Profile();
		
		BeanUtils.copyProperties(src, ret);
		
		List<Authorization> auths = src.getAuthorizations().stream()
			.map(this.authAssembler::toModel)
			.collect(Collectors.toList());
		
		ret.setAuthorizations(auths);
		
		return ret;
	}
	

	public UserEntity toEntity(User src) {
		UserEntity entity = new UserEntity();
		BeanUtils.copyProperties(src, entity);
		return entity;
	}
	
	
	public UserEntity toEntity(UserCreate src) {
		UserEntity entity = new UserEntity();
		BeanUtils.copyProperties(src, entity);
		return entity;
	}

}
