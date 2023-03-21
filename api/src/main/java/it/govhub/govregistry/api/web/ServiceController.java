package it.govhub.govregistry.api.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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
import it.govhub.govregistry.commons.api.beans.ServiceList;
import it.govhub.govregistry.commons.api.beans.ServiceOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.InternalException;
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
public class ServiceController implements ServiceApi {
	
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
	
	@Autowired
	ReadServiceController readServiceController;
	
	Logger log = LoggerFactory.getLogger(ServiceController.class);

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


	@Override
	public ResponseEntity<Void> updateServiceLogo(Long id, Resource body) {
		this.authService.hasAnyServiceAuthority(id,  GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.serviceMessages.idNotFound(id)));
		
		try {
			HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
					.currentRequestAttributes()).getRequest();
			
			service.setLogo(body.getInputStream().readAllBytes());
			service.setLogoMediaType(curRequest.getContentType());
		} catch (IOException e) {
			throw new InternalException(e);
		}
		
		this.serviceRepo.save(service);

		return ResponseEntity.ok().build();
	}


	@Override
	public ResponseEntity<Void> updateServiceLogoMiniature(Long id, Resource body) {
		this.authService.hasAnyServiceAuthority(id,  GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);

		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.serviceMessages.idNotFound(id)));
		
		try {
			HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
					.currentRequestAttributes()).getRequest();
			
			service.setLogoMiniature(body.getInputStream().readAllBytes());
			service.setLogoMiniatureMediaType(curRequest.getContentType());
		} catch (IOException e) {
			throw new InternalException(e);
		}
		
		this.serviceRepo.save(service);

		return ResponseEntity.ok().build();
	}


	@Override
	public ResponseEntity<Void> removeServiceLogo(Long id) {
		this.authService.hasAnyServiceAuthority(id,  GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.serviceMessages.idNotFound(id)));
		
		service.setLogo(null);
		service.setLogoMediaType(null);
		
		this.serviceRepo.save(service);

		return ResponseEntity.ok().build();
	}


	@Override
	public ResponseEntity<Void> removeServiceLogoMiniature(Long id) {
		this.authService.hasAnyServiceAuthority(id,  GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);

		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.serviceMessages.idNotFound(id)));
		
		service.setLogoMiniature(null);
		service.setLogoMiniatureMediaType(null);
		
		this.serviceRepo.save(service);

		return ResponseEntity.ok().build();
	}


	@Override
	public ResponseEntity<Resource> downloadServiceLogo(Long id) {
		return this.readServiceController.downloadServiceLogo(id);
	}


	@Override
	public ResponseEntity<Resource> downloadServiceLogoMiniature(Long id) {
		return this.readServiceController.downloadServiceLogoMiniature(id);
	}


	@Override
	public ResponseEntity<ServiceList> listServices(ServiceOrdering sort, Direction sortDirection, Integer limit,	Long offset, String q, List<String> withRoles) {
		return this.readServiceController.listServices(sort, sortDirection, limit, offset, q, withRoles);
	}


	@Override
	public ResponseEntity<Service> readService(Long id) {
		return this.readServiceController.readService(id);
	}

}
