package it.govhub.govregistry.commons.entity.listeners;

import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

/**
 * Gli oggetti che Ereditano da questo bean non sono memorizzabili su DB.
 * @author froggo
 *
 */
public class EntityUpdateBlocker {

	  	@PrePersist
	    void onPrePersist(Object o) {
	        throw new IllegalStateException("JPA is trying to persist an entity of type " + (o == null ? "null" : o.getClass()));
	    }

	    @PreUpdate
	    void onPreUpdate(Object o) {
	        throw new IllegalStateException("JPA is trying to update an entity of type " + (o == null ? "null" : o.getClass()));
	    }

	    @PreRemove
	    void onPreRemove(Object o) {
	        throw new IllegalStateException("JPA is trying to remove an entity of type " + (o == null ? "null" : o.getClass()));
	    }

}
