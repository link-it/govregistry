package it.govhub.govregistry.readops.api.web;

import java.util.List;
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
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.ServiceMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.ServiceAssembler;
import it.govhub.govregistry.readops.api.repository.ReadServiceRepository;
import it.govhub.govregistry.readops.api.repository.ServiceFilters;
import it.govhub.govregistry.readops.api.services.PermissionManager;
import it.govhub.govregistry.readops.api.spec.ServiceApi;
import it.govhub.security.services.SecurityService;


@V1RestController
public class ReadServiceController implements ServiceApi {
	
	@Autowired
	private ServiceAssembler serviceAssembler;
	
	@Autowired
	private ReadServiceRepository serviceRepo;
	
	@Autowired
	private SecurityService authService;
	
	@Autowired
	private PermissionManager permissionManager;
	
	
	@Override
	public ResponseEntity<ServiceList> listServices(ServiceOrdering sort, Direction sortDirection, Integer limit, Long offset, String q, List<String> haveRoles) {
		
		UserEntity principal = SecurityService.getPrincipal();
		
		Set<Long> serviceIds = this.permissionManager.listReadableServices(principal);
		
		if (haveRoles != null && !haveRoles.isEmpty()) {
			// Recupero tutti i servizi sui quali posso lavorare con i ruoli specificati
			// e faccio l'intersezione con quelle del PermissionManager.
			Set<Long> serviceIdsWithRoles = this.authService.listAuthorizedServices(haveRoles);
			serviceIds = SecurityService.restrictAuthorizations(serviceIds, serviceIdsWithRoles);
		}
		
		Specification<ServiceEntity> spec;
		
		if (serviceIds == null) {
			// Non ho restrizioni
			spec = ServiceFilters.empty();
		} else if (serviceIds.isEmpty()) {
			// Nessun servizio
			spec = ServiceFilters.never();
		} else {
			// Filtra per i servizi trovati
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
		
		Set<Long> serviceIds = this.permissionManager.listReadableServices(SecurityService.getPrincipal());
		if (serviceIds != null && !serviceIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		ServiceEntity service = this.serviceRepo.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(ServiceMessages.notFound(id)));
		
		return ResponseEntity.ok(this.serviceAssembler.toModel(service));
	}


}
