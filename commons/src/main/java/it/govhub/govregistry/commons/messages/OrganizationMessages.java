package it.govhub.govregistry.commons.messages;

import org.springframework.stereotype.Component;

@Component
public class OrganizationMessages extends RestEntityMessageBuilder {

	public OrganizationMessages() { super("Organization"); }
	
	public String conflictTaxCode(String taxCode) {
		return conflict("taxcode", taxCode);
	}
	
	public String conflictLegalName(String legalName) {
		return conflict("legalName", legalName);
	}

}
