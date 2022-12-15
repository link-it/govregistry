package it.govhub.govregistry.api.messages;

public class SecurityMessages {

	private SecurityMessages() {}
	
	public static String authorizedUserNotInDb(String principal) {
		return "The authorized user ["+principal+"] can't be found in the Database!";
	}
}
