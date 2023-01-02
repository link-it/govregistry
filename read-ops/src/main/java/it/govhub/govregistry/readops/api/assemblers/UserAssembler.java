package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.User;
import it.govhub.govregistry.commons.api.beans.UserCreate;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.readops.api.spec.UserApi;

@Component
public class UserAssembler  extends RepresentationModelAssemblerSupport<UserEntity, User> {
	

	public UserAssembler() {
		super(UserApi.class, User.class);
	}

	@Override
	public User toModel(UserEntity src) {
		User ret = instantiateModel(src);
		
        BeanUtils.copyProperties(src, ret);

		ret.add(linkTo(
					methodOn(UserApi.class)
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
