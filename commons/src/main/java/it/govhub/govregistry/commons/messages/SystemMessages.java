package it.govhub.govregistry.commons.messages;

public class SystemMessages {

	public static String internalError() {
		return "Request can't be satisfied at the moment";
	}
	
	public static String endpointNotFound() {
		return "Resource not found."; 
	}
	
	public static String sessionExpired() {
		return "This session has been expired (possibly due to multiple concurrent logins being attempted as the same user)";
	}
	

}
