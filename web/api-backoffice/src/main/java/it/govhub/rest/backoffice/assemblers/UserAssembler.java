package it.govhub.rest.backoffice.assemblers;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.web.UsersController;

@Component
public class UserAssembler  extends RepresentationModelAssemblerSupport<UserEntity, User> {

	public UserAssembler() {
		super(UsersController.class, User.class);
	}

	@Override
	public User toModel(UserEntity src) {
		User ret = instantiateModel(src);
		
		ret.setFullName(src.getFull_name());
		ret.setEnabled(src.getEnabled());
		ret.setEmail(src.getEmail());
		ret.setId(src.getId());
		ret.setPrincipal(src.getPrincipal());
		
		// TODO Aggiungi links
		
		return ret;
	}


}
