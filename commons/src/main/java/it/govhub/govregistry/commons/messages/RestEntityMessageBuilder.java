package it.govhub.govregistry.commons.messages;

import java.util.Collection;

public class RestEntityMessageBuilder {

	private String entityName;
	
	
	public RestEntityMessageBuilder(String entityName) {
		this.entityName = entityName;
	}
	
	
	public String notFound(String key, Object keyValue) {
		return this.entityName + " with " + key + " [" + keyValue + "] Not Found."; 
	}
	
	public String conflict() {
		return this.entityName + " already present";
	}
	
	
	public String conflict(String key, Object keyValue) {
		return this.entityName + " with " + key + " [" + keyValue + "] Already Exists.";
	}
	
	public String idNotFound(Long id) {
		return notFound("id", id);
	}
	
	public String idsNotFound(Collection<Long> ids) {
		return this.entityName + " with ids IN [" + ids + "] Not Found.";
	}
	
	public String fieldNotModificable(String key) {
		return this.entityName + " field ["+key+"] is not modificable.";
	}
	
}
