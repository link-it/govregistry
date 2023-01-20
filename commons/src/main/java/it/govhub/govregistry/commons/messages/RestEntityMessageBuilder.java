package it.govhub.govregistry.commons.messages;

public class RestEntityMessageBuilder {

	private String entityName;
	
	
	public RestEntityMessageBuilder(String entityName) {
		this.entityName = entityName;
	}
	
	
	public String notFound( String key, Object keyValue) {
		return this.entityName + "with " + key + "[" + keyValue + "] Not Found."; 
	}
	
	
	public String conflict( String key, Object keyValue) {
		return this.entityName + "with " + key + "[" + keyValue + "] Already Exists.";
	}
	
	public String idNotFound(Long id) {
		return notFound("id", id);
	}
	
}
