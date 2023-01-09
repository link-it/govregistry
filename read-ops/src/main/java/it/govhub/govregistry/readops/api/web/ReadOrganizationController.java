package it.govhub.govregistry.readops.api.web;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationList;
import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAssembler;
import it.govhub.govregistry.readops.api.assemblers.OrganizationItemAssembler;
import it.govhub.govregistry.readops.api.repository.OrganizationFilters;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.services.PermissionManager;
import it.govhub.govregistry.readops.api.spec.OrganizationApi;
import it.govhub.security.services.SecurityService;


@V1RestController
public class ReadOrganizationController implements OrganizationApi {
	
	@Autowired
	OrganizationAssembler orgAssembler;
	
	@Autowired
	OrganizationItemAssembler orgItemAssembler;
	
	@Autowired
	ReadOrganizationRepository orgRepo;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	PermissionManager permissionManager;
	
	
	@Override
	public ResponseEntity<OrganizationList> listOrganizations(OrganizationOrdering sort, Direction sortDirection, Integer limit, Long offset, String q, List<String> haveRoles) {
		
		UserEntity principal = SecurityService.getPrincipal();
		
		Set<Long> orgIds = this.permissionManager.listReadableOrganizations(principal);
		
		if (haveRoles != null && !haveRoles.isEmpty()) {
			// Recupero Tutte le organizzazioni sulle quali posso lavorare con i ruoli specificati
			// e faccio l'intersezione con quelle del PermissionManager.
			Set<Long> orgIdsWithRoles = this.authService.listAuthorizedOrganizations(haveRoles);
			orgIds = SecurityService.restrictAuthorizations(orgIds, orgIdsWithRoles);
		}

		Specification<OrganizationEntity> spec;
		
		if (orgIds == null) {
			// Non ho restrizioni
			spec = OrganizationFilters.empty();
		} else if (orgIds.isEmpty()) {
			// Nessuna organizzazione
			spec = OrganizationFilters.never();
		} else {
			// Filtra per le organizzazioni trovate
			spec = OrganizationFilters.byId(orgIds);
		}
		
		if (q != null) {
			spec = spec.and(
					OrganizationFilters.likeTaxCode(q).or(OrganizationFilters.likeLegalName(q)));
		}
		
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, OrganizationFilters.sort(sort, sortDirection));
		
		Page<OrganizationEntity> organizations = this.orgRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		OrganizationList ret = ListaUtils.buildPaginatedList(organizations, pageRequest.limit, curRequest, new OrganizationList());
		for (OrganizationEntity org : organizations) {
			ret.addItemsItem(this.orgItemAssembler.toModel(org));
		}
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<Organization> readOrganization(Long id) {
		
		Set<Long> orgIds = this.permissionManager.listReadableOrganizations(SecurityService.getPrincipal());
		if (orgIds != null && !orgIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		Organization ret = this.orgRepo.findById(id)
			.map( org -> this.orgAssembler.toModel(org))
			.orElseThrow( () -> new ResourceNotFoundException(OrganizationMessages.notFound(id)));
		
		return ResponseEntity.ok(ret);
	}


	@Override
	public ResponseEntity<Resource> downloadOrganizationLogo(Long id) {
		
		Set<Long> orgIds = this.permissionManager.listReadableOrganizations(SecurityService.getPrincipal());
		if (orgIds != null && !orgIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(OrganizationMessages.notFound(id)));
		
		byte[] ret = org.getLogo() != null ? org.getLogo() : new byte[0];
		
		ByteArrayInputStream bret = new ByteArrayInputStream(ret);
		
		InputStreamResource logoStream = new InputStreamResource(bret);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(ret.length);
		
		ResponseEntity<Resource> ret2 =   new ResponseEntity<>(logoStream, headers, HttpStatus.OK); 
		
		return ret2;
	}


	@Override
	public ResponseEntity<Resource> downloadOrganizationLogoMiniature(Long id) {
		
		Set<Long> orgIds = this.permissionManager.listReadableOrganizations(SecurityService.getPrincipal());
		if (orgIds != null && !orgIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(OrganizationMessages.notFound(id)));
		
		byte[] ret = org.getLogoMiniature() != null ? org.getLogoMiniature() : new byte[0];
		
		ByteArrayInputStream bret = new ByteArrayInputStream(ret);
		
		InputStreamResource logoStream = new InputStreamResource(bret);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(ret.length);
		
		ResponseEntity<Resource> ret2 =   new ResponseEntity<>(logoStream, headers, HttpStatus.OK); 
		
		return ret2;
	}

}
