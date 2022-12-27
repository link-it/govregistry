package it.govhub.govregistry.commons.messages;

public class OrganizationMessages {

	private OrganizationMessages() {}
	
	public static String conflictTaxCode(String taxCode) {
		return "Organization with taxCode ["+taxCode+"] already exists.";
	}
	
	public static String conflictLegalName(String legalName) {
		return "Organizzazione with legalName ["+legalName+"] already exists.";
	}

	public static String notFound(Long id) {
		return "Organization with id  ["+id+"] not found.";
	}
}
