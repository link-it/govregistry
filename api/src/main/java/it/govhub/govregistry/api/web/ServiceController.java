package it.govhub.govregistry.api.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import it.govhub.govregistry.api.repository.ServiceRepository;
import it.govhub.govregistry.api.services.ServiceService;
import it.govhub.govregistry.api.spec.ServiceApi;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceCreate;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.PatchMessages;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.govregistry.readops.api.assemblers.ServiceAssembler;
import it.govhub.govregistry.readops.api.web.ReadServiceController;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@V1RestController
public class ServiceController extends ReadServiceController implements ServiceApi {
	
	@Autowired
	ServiceService serviceService;
	
	@Autowired
	ServiceAssembler serviceAssembler;

	@Autowired
	SecurityService authService;
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	Validator validator;
	
	@Autowired
	ServiceRepository serviceRepo;
	
	@Autowired
	ServiceMessages serviceMessages;
	
	Logger log = LoggerFactory.getLogger(ServiceController.class);
	

	private static Set<String> readServiceRoles = Set.of(
			GovregistryRoles.GOVREGISTRY_SYSADMIN ,
			GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR,
			GovregistryRoles.GOVREGISTRY_SERVICES_VIEWER);
			
	
	@Override
	protected Set<String> getReadServiceRoles() {
		return new HashSet<>(readServiceRoles);
	}
	
	
	@Override
	public ResponseEntity<Service> createService(ServiceCreate serviceCreate) {
		PostgreSQLUtilities.throwIfContainsNullByte(serviceCreate.getServiceName(), "service_name");
		PostgreSQLUtilities.throwIfContainsNullByte(serviceCreate.getDescription(), "description");
		
		this.authService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR);
	
		ServiceEntity newService = this.serviceAssembler.toEntity(serviceCreate);
		newService = this.serviceService.createService(newService);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(this.serviceAssembler.toModel(newService));
	}
	
	
	@Override
	public ResponseEntity<Service> updateService(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyServiceAuthority(id,  GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		log.info("Patching service [{id}}: {}", patch);
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.serviceMessages.idNotFound(id)));
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		Service restService = this.serviceAssembler.toModel(service);
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
		
		newService = this.serviceService.replaceService(service, newService);
		
		return ResponseEntity.ok(
				this.serviceAssembler.toModel(newService));
	}

}
