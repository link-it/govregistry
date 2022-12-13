package it.govhub.govregistry.api.messages;

public class RoleMessages {

	private RoleMessages() {}
	
	public static String notFound(Long id) {
		return "Role with ID ["+id+"] not found.";
	}
	
	public static String authorizationNotFound(Long id) {
		return "Authorization with ID ["+id+"] not found.";
	}

}
