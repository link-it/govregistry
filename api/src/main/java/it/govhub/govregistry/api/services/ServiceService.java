package it.govhub.govregistry.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import it.govhub.govregistry.api.messages.PatchMessages;
import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceCreate;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.readops.api.assemblers.ServiceAssembler;
import it.govhub.govregistry.readops.api.repository.ServiceRepository;

@org.springframework.stereotype.Service
public class ServiceService {
	
	@Autowired
	private ServiceRepository serviceRepo;
	
	@Autowired
	private ServiceAssembler serviceAssembler;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private Validator validator;
	
	public ServiceEntity createService(ServiceCreate service) {
	
		ServiceEntity newService = this.serviceAssembler.toEntity(service);
		
		return this.serviceRepo.save(newService);
	}

	
	public ServiceEntity patchService(Long id, JsonPatch patch) {
		
		ServiceEntity user = this.serviceRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(ServiceMessages.notFound(id)));
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		Service restService = this.serviceAssembler.toModel(user);
		JsonNode jsonService = this.objectMapper.convertValue(restService, JsonNode.class);
		
		JsonNode newJsonService;
		try {
			newJsonService= patch.apply(jsonService);
		} catch (JsonPatchException e) {			
			throw new BadRequestException(e.getLocalizedMessage());
		}
		
		// Lo converto nell'oggetto User, sostituendo l'ID per essere sicuri che la patch
		// non l'abbia cambiato.
		ServiceCreate updatedService;
		try {
			updatedService = this.objectMapper.treeToValue(newJsonService, ServiceCreate.class);
		} catch (JsonProcessingException e) {
			throw new BadRequestException(e);
		}
		
		if (updatedService == null) {
			throw new BadRequestException(PatchMessages.VOID_OBJECT_PATCH);
		}
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedService, updatedService.getClass().getName());
		validator.validate(updatedService, errors);
		if (!errors.getAllErrors().isEmpty()) {
			throw new BadRequestException(PatchMessages.validationFailed(errors));
		}
		
		// Faccio partire la validazione custom per la stringa \u0000
		PostgreSQLUtilities.throwIfContainsNullByte(updatedService.getServiceName(), "service_name");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedService.getDescription(), "description");

		// Dall'oggetto REST passo alla entity
		ServiceEntity  newService = this.serviceAssembler.toEntity(updatedService);
		newService.setId(id);
				
		return this.serviceRepo.save(newService);
	}


}
