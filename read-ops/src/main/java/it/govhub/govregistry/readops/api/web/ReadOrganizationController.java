package it.govhub.govregistry.readops.api.web;

import java.io.ByteArrayInputStream;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.exception.NotAuthorizedException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAssembler;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.spec.OrganizationApi;
import it.govhub.security.services.SecurityService;


/**
 *		Controller comune per le operazioni di lettura sulle organizzazioni.
 *
 *		I servizi di govhub implementano questo controller e forniscono l'override della getReadOrganizationRoles
 *		che dice per quel servizio quali sono i ruoli che consentono la lettura delle organizzazioni. 
 *
 */
public abstract class ReadOrganizationController implements OrganizationApi {
	
	@Autowired
	OrganizationAssembler orgAssembler;
	
	@Autowired
	ReadOrganizationRepository orgRepo;
	
	@Autowired
	SecurityService authService;	
	
	protected abstract Set<String> getReadOrganizationRoles();	

	@Override
	public ResponseEntity<Organization> readOrganization(Long id) {
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(getReadOrganizationRoles());
		
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
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(getReadOrganizationRoles());
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
		
		Set<Long> orgIds = this.authService.listAuthorizedOrganizations(getReadOrganizationRoles());
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
