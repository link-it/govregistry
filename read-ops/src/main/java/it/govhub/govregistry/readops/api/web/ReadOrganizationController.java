package it.govhub.govregistry.readops.api.web;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationList;
import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.config.ApplicationConfig;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAssembler;
import it.govhub.govregistry.readops.api.assemblers.OrganizationItemAssembler;
import it.govhub.govregistry.readops.api.repository.OrganizationFilters;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.security.services.SecurityService;


/**
 *		Controller comune per le operazioni di lettura sulle organizzazioni.
 *
 *		I servizi di govhub implementano questo controller e forniscono l'override della getReadOrganizationRoles
 *		che dice per quel servizio quali sono i ruoli che consentono la lettura delle organizzazioni. 
 *
 */
@RequestMapping("/v1")
@Component
public class ReadOrganizationController {
	
	@Autowired
	OrganizationAssembler orgAssembler;
	
	@Autowired
	ReadOrganizationRepository orgRepo;
	
	@Autowired
	SecurityService authService;	
	
	@Autowired
	OrganizationMessages orgMessages;
	
	@Autowired
	OrganizationItemAssembler orgItemAssembler;
	
	@Autowired
	ApplicationConfig applicationConfig;
	
	public ResponseEntity<Organization> readOrganization(Long id) {
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(this.applicationConfig.getReadOrganizationRoles());
		
		if (orgIds != null && !orgIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		Organization ret = this.orgRepo.findById(id)
			.map( org -> this.orgAssembler.toModel(org))
			.orElseThrow( () -> new ResourceNotFoundException(this.orgMessages.idNotFound(id)));
		
		return ResponseEntity.ok(ret);
	}

    public ResponseEntity<OrganizationList> listOrganizations(
            OrganizationOrdering sort,
            Direction sortDirection,
            Integer limit,
            Long offset,
            String q,
            List<String> withRoles
        ) {
		Set<String> roles = new HashSet<>(this.applicationConfig.getReadOrganizationRoles());
		
		if (withRoles != null) {
			roles.retainAll(withRoles);
		}
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(roles);

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
			spec = spec.and(OrganizationFilters.likeTaxCode(q).or(OrganizationFilters.likeLegalName(q)));
		}

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit,
				OrganizationFilters.sort(sort, sortDirection));

		Page<OrganizationEntity> organizations = this.orgRepo.findAll(spec, pageRequest.pageable);

		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();

		OrganizationList ret = ListaUtils.buildPaginatedList(organizations, pageRequest.limit, curRequest,
				new OrganizationList());
				
		for (OrganizationEntity org : organizations) {
			ret.addItemsItem(this.orgItemAssembler.toModel(org));
		}

		return ResponseEntity.ok(ret);
		
	}


	public ResponseEntity<Resource> downloadOrganizationLogo(Long id) {
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(this.applicationConfig.getReadOrganizationRoles());
		if (orgIds != null && !orgIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.orgMessages.idNotFound(id)));
		
		if (org.getLogo() == null) {
			throw new ResourceNotFoundException();
		}
		
		byte[] logoBytes = org.getLogo();
		ByteArrayInputStream bret = new ByteArrayInputStream(logoBytes);
		InputStreamResource logoStream = new InputStreamResource(bret);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(logoBytes.length);
		headers.setContentType(MediaType.valueOf(org.getLogoMediaType()));
		
		ResponseEntity<Resource> ret =   new ResponseEntity<>(logoStream, headers, HttpStatus.OK); 
		return ret;
	}


	public ResponseEntity<Resource> downloadOrganizationLogoMiniature(Long id) {
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(this.applicationConfig.getReadOrganizationRoles());
		if (orgIds != null && !orgIds.contains(id)) {
			throw new NotAuthorizedException();
		}
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.orgMessages.idNotFound(id)));
		
		if (org.getLogoMiniature() == null) {
			throw new ResourceNotFoundException();
		}
		
		byte[] ret = org.getLogoMiniature();
		ByteArrayInputStream bret = new ByteArrayInputStream(ret);
		InputStreamResource logoStream = new InputStreamResource(bret);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(ret.length);
		headers.setContentType(MediaType.valueOf(org.getLogoMiniatureMediaType()));

		
		ResponseEntity<Resource> ret2 =   new ResponseEntity<>(logoStream, headers, HttpStatus.OK); 
		return ret2;
	}

}
