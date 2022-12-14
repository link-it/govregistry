package it.govhub.govregistry.api.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import it.govhub.govregistry.api.assemblers.OrganizationAssembler;
import it.govhub.govregistry.api.beans.Organization;
import it.govhub.govregistry.api.beans.OrganizationCreate;
import it.govhub.govregistry.api.messages.OrganizationMessages;
import it.govhub.govregistry.api.messages.PatchMessages;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.ConflictException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.repository.OrganizationRepository;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;

@Service
public class OrganizationService {

	@Autowired
	private OrganizationAssembler orgAssembler;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private Validator validator;
	
	@Transactional
	public OrganizationEntity createOrganization(OrganizationCreate org) {

		if (this.orgRepo.findByTaxCode(org.getTaxCode()).isPresent()) {
			throw new ConflictException(OrganizationMessages.conflictTaxCode(org.getTaxCode()));
		}
		
		if (this.orgRepo.findByLegalName(org.getLegalName()).isPresent()) {
			throw new ConflictException(OrganizationMessages.conflictLegalName(org.getLegalName()));
		}

		OrganizationEntity toCreate = this.orgAssembler.toEntity(org);
		return this.orgRepo.save(toCreate);
	}
	
	
	@Transactional
	public OrganizationEntity patchOrganization(Long id, JsonPatch patch) {
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(OrganizationMessages.notFound(id)));
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		Organization restOrg = this.orgAssembler.toModel(org);
		
		JsonNode newJsonOrg;
		try {
			JsonNode jsonOrg = this.objectMapper.convertValue(restOrg, JsonNode.class);
			newJsonOrg= patch.apply(jsonOrg);
		} catch (JsonPatchException e) {			
			throw new BadRequestException(e.getLocalizedMessage());
		}
		
		// Lo converto nell'oggetto User, sostituendo l'ID per essere sicuri che la patch
		// non l'abbia cambiato.
		OrganizationCreate updatedOrganization;
		try {
			updatedOrganization = this.objectMapper.treeToValue(newJsonOrg, OrganizationCreate.class);
		} catch (JsonProcessingException e) {
			throw new BadRequestException(e);
		}
		
		if (updatedOrganization == null) {
			throw new BadRequestException(PatchMessages.VOID_OBJECT_PATCH);
		}
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedOrganization, updatedOrganization.getClass().getName());
		validator.validate(updatedOrganization, errors);
		if (!errors.getAllErrors().isEmpty()) {
			throw new BadRequestException(PatchMessages.validationFailed(errors));
		}
		
		// Faccio partire la validazione custom per la stringa \u0000
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeAddress(), "office_address");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeAddressDetails(), "office_address_details");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeAt(), "office_at");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeForeignState(), "office_foreign_state");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeMunicipality(), "office_municipality");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeMunicipalityDetails(), "office_municipality_details");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeProvince(), "office_province");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeZip(), "office_zip");
		
		
		// Dall'oggetto REST passo alla entity
		OrganizationEntity newOrg = this.orgAssembler.toEntity(updatedOrganization);
		newOrg.setId(id);
		
		// Controllo eventuali conflitti sugli indici
		Optional<OrganizationEntity> conflictOrg = this.orgRepo.findByTaxCode(newOrg.getTaxCode())
				.filter( o -> !o.getId().equals(newOrg.getId()));
		
		if (conflictOrg.isPresent() ) {
			throw new ConflictException(OrganizationMessages.conflictTaxCode(newOrg.getTaxCode()));
		}
		
		 conflictOrg = this.orgRepo.findByLegalName(newOrg.getLegalName())
					.filter( o -> !o.getId().equals(newOrg.getId()));
			
			if (conflictOrg.isPresent() ) {
				throw new ConflictException(OrganizationMessages.conflictLegalName(newOrg.getLegalName()));
			}
				
		return this.orgRepo.save(newOrg);
		
	}


}