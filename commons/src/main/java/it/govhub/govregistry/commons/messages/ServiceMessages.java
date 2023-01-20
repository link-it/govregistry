package it.govhub.govregistry.commons.messages;

import org.springframework.stereotype.Component;

@Component
public class ServiceMessages extends RestEntityMessageBuilder{

		public ServiceMessages() { super("Service"); }
}
