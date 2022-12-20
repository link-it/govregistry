package it.govhub.govregistry.api.web;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.govregistry.api.assemblers.ServiceAssembler;
import it.govhub.govregistry.api.beans.Service;
import it.govhub.govregistry.api.beans.ServiceCreate;
import it.govhub.govregistry.api.beans.ServiceList;
import it.govhub.govregistry.api.beans.ServiceOrdering;
import it.govhub.govregistry.api.messages.ServiceMessages;
import it.govhub.govregistry.api.repository.ServiceFilters;
import it.govhub.govregistry.api.services.ServiceService;
import it.govhub.govregistry.api.spec.ServiceApi;
import it.govhub.govregistry.commons.beans.PatchOp;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.repository.ServiceRepository;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@RestController
public class ServiceController implements ServiceApi {
	
	@Autowired
	private ServiceService serviceService;
	
	@Autowired
	private ServiceAssembler serviceAssembler;
	
	@Autowired
	private ServiceRepository serviceRepo;
	
	@Autowired
	private SecurityService authService;
	
	
	@Override
	public ResponseEntity<Service> createService(ServiceCreate serviceCreate) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_EDITOR);
		
		PostgreSQLUtilities.throwIfContainsNullByte(serviceCreate.getServiceName(), "service_name");
		PostgreSQLUtilities.throwIfContainsNullByte(serviceCreate.getDescription(), "description");
		
		ServiceEntity newService = this.serviceService.createService(serviceCreate);
		
		Service ret = this.serviceAssembler.toModel(newService);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	
	@Override
	public ResponseEntity<ServiceList> listServices(ServiceOrdering sort, Direction sortDirection, Integer limit, Long offset, String q) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_VIEWER, GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_EDITOR);
		
		
		Specification<ServiceEntity> spec;
		
		if (this.authService.canReadAllServices()) {
			spec = ServiceFilters.empty();
		} else {
			Set<Long> serviceIds = this.authService.listAuthorizedServices(
					GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_EDITOR, GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_VIEWER);
			
			spec = ServiceFilters.byId(serviceIds);
		}
		
		if (q != null) {
			spec = spec.and(
					ServiceFilters.likeDescription(q).or(ServiceFilters.likeName(q)));
		}

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, ServiceFilters.sort(sort,sortDirection));
		
		Page<ServiceEntity> services = this.serviceRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		ServiceList ret = ListaUtils.costruisciListaPaginata(services, pageRequest.limit, curRequest, new ServiceList());
		
		for (ServiceEntity service : services) {
			ret.addItemsItem(this.serviceAssembler.toModel(service));
		}
		
		return ResponseEntity.ok(ret);
	}


	@Override
	public ResponseEntity<Service> readService(Long id) {
		
		this.authService.hasAnyServiceAuthority(id, GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_VIEWER, GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_EDITOR);
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(ServiceMessages.notFound(id)));
		
		return ResponseEntity.ok(this.serviceAssembler.toModel(service));
	}


	@Override
	public ResponseEntity<Service> updateService(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyServiceAuthority(id,  GovregistryRoles.RUOLO_GOVREGISTRY_SERVICES_EDITOR);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		ServiceEntity updated = this.serviceService.patchService(id, patch);
		
		return ResponseEntity.ok(
				this.serviceAssembler.toModel(updated));
	}

}
