package it.govhub.govregistry.api.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.govregistry.api.services.ServiceService;
import it.govhub.govregistry.api.spec.ServiceApi;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceCreate;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.govregistry.readops.api.assemblers.ServiceAssembler;
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
	
	
	@Override
	public ResponseEntity<Service> createService(ServiceCreate serviceCreate) {
		
		this.authService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR);
		
		PostgreSQLUtilities.throwIfContainsNullByte(serviceCreate.getServiceName(), "service_name");
		PostgreSQLUtilities.throwIfContainsNullByte(serviceCreate.getDescription(), "description");
		
		ServiceEntity newService = this.serviceService.createService(serviceCreate);
		
		Service ret = this.serviceAssembler.toModel(newService);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	
	
	@Override
	public ResponseEntity<Service> updateService(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyServiceAuthority(id,  GovregistryRoles.GOVREGISTRY_SERVICES_EDITOR);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		ServiceEntity updated = this.serviceService.patchService(id, patch);
		
		return ResponseEntity.ok(
				this.serviceAssembler.toModel(updated));
	}

}
