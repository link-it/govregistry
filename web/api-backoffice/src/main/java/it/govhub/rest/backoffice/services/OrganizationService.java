package it.govhub.rest.backoffice.services;

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

import it.govhub.rest.backoffice.assemblers.OrganizationAssembler;
import it.govhub.rest.backoffice.beans.Organization;
import it.govhub.rest.backoffice.beans.OrganizationCreate;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.BadRequestException;
import it.govhub.rest.backoffice.exception.ConflictException;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.exception.SemanticValidationException;
import it.govhub.rest.backoffice.repository.OrganizationRepository;
import it.govhub.rest.backoffice.utils.PostgreSQLUtilities;
import it.govhub.rest.backoffice.utils.RequestUtils;

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
			throw new ConflictException("Organizzazione con taxCode ["+org.getTaxCode()+"] già presente.");
		}
		
		if (this.orgRepo.findByLegalName(org.getLegalName()).isPresent()) {
			throw new ConflictException("Organizzazione con legalName ["+org.getLegalName()+"] già presente.");
		}

		OrganizationEntity toCreate = this.orgAssembler.toEntity(org);
		return this.orgRepo.save(toCreate);
	}
	
	
	@Transactional
	public OrganizationEntity patchOrganization(Long id, JsonPatch patch) {
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException("Organizzazione di id ["+id+"] non trovata."));
		
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
			throw new BadRequestException("PATCH non valida, risulterebbe in un oggetto nullo.");
		}
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedOrganization, updatedOrganization.getClass().getName());
		validator.validate(updatedOrganization, errors);
		if (!errors.getAllErrors().isEmpty()) {
			String msg = "L'oggetto patchato viola i vincoli dello schema: " + RequestUtils.extractValidationError(errors.getAllErrors().get(0));
			throw new BadRequestException(msg);
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
			throw new ConflictException("Organizzazione con taxCode ["+org.getTaxCode()+"] già presente.");
		}
		
		 conflictOrg = this.orgRepo.findByLegalName(newOrg.getLegalName())
					.filter( o -> !o.getId().equals(newOrg.getId()));
			
			if (conflictOrg.isPresent() ) {
				throw new ConflictException("Organizzazione con taxCode ["+org.getTaxCode()+"] già presente.");
			}
				
		return this.orgRepo.save(newOrg);
		
	}


}
