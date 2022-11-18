package it.govhub.rest.backoffice.web;

import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_SERVICES_EDITOR;
import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_SERVICES_VIEWER;
import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_SYSADMIN;

import java.util.List;

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

import it.govhub.rest.backoffice.api.ServiceApi;
import it.govhub.rest.backoffice.assemblers.ServiceAssembler;
import it.govhub.rest.backoffice.beans.PatchOp;
import it.govhub.rest.backoffice.beans.Service;
import it.govhub.rest.backoffice.beans.ServiceCreate;
import it.govhub.rest.backoffice.beans.ServiceList;
import it.govhub.rest.backoffice.beans.ServiceOrdering;
import it.govhub.rest.backoffice.entity.ServiceEntity;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.messages.ServiceMessages;
import it.govhub.rest.backoffice.repository.ServiceFilters;
import it.govhub.rest.backoffice.repository.ServiceRepository;
import it.govhub.rest.backoffice.services.SecurityService;
import it.govhub.rest.backoffice.services.ServiceService;
import it.govhub.rest.backoffice.utils.LimitOffsetPageRequest;
import it.govhub.rest.backoffice.utils.ListaUtils;
import it.govhub.rest.backoffice.utils.PostgreSQLUtilities;
import it.govhub.rest.backoffice.utils.RequestUtils;

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
		
		this.authService.hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_SERVICES_EDITOR);
		
		PostgreSQLUtilities.throwIfContainsNullByte(serviceCreate.getServiceName(), "service_name");
		PostgreSQLUtilities.throwIfContainsNullByte(serviceCreate.getDescription(), "description");
		
		ServiceEntity newService = this.serviceService.createService(serviceCreate);
		
		Service ret = this.serviceAssembler.toModel(newService);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	
	@Override
	public ResponseEntity<ServiceList> listServices(ServiceOrdering sort, Direction sortDirection, Integer limit, Long offset, String q) {
		
		this.authService.hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_SERVICES_VIEWER, RUOLO_GOVHUB_SERVICES_EDITOR);
		
		Specification<ServiceEntity> spec = ServiceFilters.empty();
		if (q != null) {
			spec = ServiceFilters.likeDescription(q).or(ServiceFilters.likeName(q));
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
		
		this.authService.hasAnyServiceAuthority(id, RUOLO_GOVHUB_SERVICES_VIEWER, RUOLO_GOVHUB_SERVICES_EDITOR);
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(ServiceMessages.notFound(id)));
		
		return ResponseEntity.ok(this.serviceAssembler.toModel(service));
	}


	@Override
	public ResponseEntity<Service> updateService(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyServiceAuthority(id,  RUOLO_GOVHUB_SERVICES_EDITOR);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		ServiceEntity updated = this.serviceService.patchService(id, patch);
		
		return ResponseEntity.ok(
				this.serviceAssembler.toModel(updated));
	}

}
