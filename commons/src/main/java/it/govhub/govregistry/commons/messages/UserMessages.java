package it.govhub.govregistry.commons.messages;

import org.springframework.stereotype.Component;

@Component
public class UserMessages extends RestEntityMessageBuilder {
	
	public UserMessages() {
		super("User");
	}

	public String conflictPrincipal(String principal) {
		return conflict("principal", principal); 
	}
	
	
	public String principalNotFound(String principal) {
		return notFound("princpal", principal);
	}

}
