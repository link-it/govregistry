package it.govhub.govregistry.api.messages;

public class ServiceMessages {

		private ServiceMessages() {}
		
		public static String notFound(Long id) {
			return "Service with ID ["+id+"] not found.";
		}
		
}
