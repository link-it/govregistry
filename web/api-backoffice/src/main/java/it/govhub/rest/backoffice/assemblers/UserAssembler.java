package it.govhub.rest.backoffice.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.web.UserController;

@Component
public class UserAssembler  extends RepresentationModelAssemblerSupport<UserEntity, User> {

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
