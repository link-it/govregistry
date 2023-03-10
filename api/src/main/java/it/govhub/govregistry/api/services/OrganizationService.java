package it.govhub.govregistry.api.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.exception.ConflictException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAssembler;

@Service
public class OrganizationService {

	@Autowired
	OrganizationAssembler orgAssembler;
	
	@Autowired
	OrganizationRepository orgRepo;
	
	@Autowired
	OrganizationMessages orgMessages;
	
	Logger log = LoggerFactory.getLogger(OrganizationService.class);


	@Transactional
	public OrganizationEntity createOrganization(OrganizationEntity org) {
		log.info("Creating new organization: {}", org);
		
		if (this.orgRepo.findByTaxCode(org.getTaxCode()).isPresent()) {
			throw new ConflictException(this.orgMessages.conflictTaxCode(org.getTaxCode()));
		}
		
		if (this.orgRepo.findByLegalName(org.getLegalName()).isPresent()) {
			throw new ConflictException(this.orgMessages.conflictLegalName(org.getLegalName()));
		}

		return this.orgRepo.save(org);
	}
	

	@Transactional
	public OrganizationEntity replaceOrganization(OrganizationEntity oldOrg, OrganizationEntity newOrg) {
		log.info("Replacing Organization {} With {}", oldOrg, newOrg);

		if (oldOrg.getId() != newOrg.getId()) {
			throw new SemanticValidationException(this.orgMessages.fieldNotModificable("id"));
		}
		
		// Controllo eventuali conflitti sugli indici
		Optional<OrganizationEntity> conflictOrg = this.orgRepo.findByTaxCode(newOrg.getTaxCode())
				.filter( o -> !o.getId().equals(newOrg.getId()));
		
		if (conflictOrg.isPresent() ) {
			throw new ConflictException(this.orgMessages.conflictTaxCode(newOrg.getTaxCode()));
		}
		
		 conflictOrg = this.orgRepo.findByLegalName(newOrg.getLegalName())
					.filter( o -> !o.getId().equals(newOrg.getId()));
			
			if (conflictOrg.isPresent() ) {
				throw new ConflictException(this.orgMessages.conflictLegalName(newOrg.getLegalName()));
			}
		
		return this.orgRepo.save(newOrg);
	}
	
	
}
