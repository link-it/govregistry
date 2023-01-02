package it.govhub.govregistry.readops.api.web;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceList;
import it.govhub.govregistry.commons.api.beans.ServiceOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.ServiceAssembler;
import it.govhub.govregistry.readops.api.repository.ServiceFilters;
import it.govhub.govregistry.readops.api.repository.ServiceRepository;
import it.govhub.govregistry.readops.api.spec.ServiceApi;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;


@V1RestController
public class ReadServiceController implements ServiceApi {
	
	@Autowired
	private ServiceAssembler serviceAssembler;
	
	@Autowired
	private ServiceRepository serviceRepo;
	
	@Autowired
	private SecurityService authService;
	
	
	
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


}
