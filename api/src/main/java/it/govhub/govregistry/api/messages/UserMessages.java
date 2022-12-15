package it.govhub.govregistry.api.messages;

public class UserMessages {
	
	private UserMessages() {}

	public static String conflictPrincipal(String principal) {
		return "User with principal ["+principal+"] already exists.";
	}
	
	public static String notFound(Long id) {
		return "User with id  ["+id+"] not found.";
	}
	
	
	public static String notFound(String principal) {
		return "User with principal ["+principal+"] not found.";
	}

}
