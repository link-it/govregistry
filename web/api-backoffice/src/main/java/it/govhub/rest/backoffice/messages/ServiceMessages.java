package it.govhub.rest.backoffice.messages;

public class ServiceMessages {

		private ServiceMessages() {}
		
		public static String notFound(Long id) {
			return "Service with ID ["+id+"] not found.";
		}
		
}