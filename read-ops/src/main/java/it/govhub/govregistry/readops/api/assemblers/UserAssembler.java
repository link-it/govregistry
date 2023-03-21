package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.User;
import it.govhub.govregistry.commons.api.beans.UserCreate;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.web.ReadUserController;

@Component
public class UserAssembler  extends RepresentationModelAssemblerSupport<UserEntity, User> {
	
	Logger log = LoggerFactory.getLogger(UserAssembler.class);

	public UserAssembler() {
		super(ReadUserController.class, User.class);
	}

	@Override
	public User toModel(UserEntity src) {
		log.debug("Assembling Entity [User] to model...");
		User ret = instantiateModel(src);
		
        BeanUtils.copyProperties(src, ret);

		ret.add(linkTo(
					methodOn(ReadUserController.class)
					.readUser(src.getId()))
				.withSelfRel()
			);
		
		if (src.getEmail() != null) {
			String md5 = DigestUtils.md5Hex(src.getEmail());
			ret.add(
					Link.of("https://gravatar.com/avatar/"+md5+"/s=100&d=identicon", LinkRelation.of("avatar")));
		}
		
		return ret;
	}
	

	public UserEntity toEntity(User src) {
		log.debug("Converting Model [User] to entity...");
		UserEntity entity = new UserEntity();
		BeanUtils.copyProperties(src, entity);
		return entity;
	}
	
	
	public UserEntity toEntity(UserCreate src) {
		log.debug("Converting Model [UserCreate] to entity...");
		UserEntity entity = new UserEntity();
		BeanUtils.copyProperties(src, entity);
		return entity;
	}

}
