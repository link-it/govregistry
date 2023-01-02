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

import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationList;
import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAssembler;
import it.govhub.govregistry.readops.api.assemblers.OrganizationItemAssembler;
import it.govhub.govregistry.readops.api.repository.OrganizationFilters;
import it.govhub.govregistry.readops.api.repository.OrganizationRepository;
import it.govhub.govregistry.readops.api.spec.OrganizationApi;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;


@V1RestController
public class ReadOrganizationController implements OrganizationApi {
	
	@Autowired
	OrganizationAssembler orgAssembler;
	
	@Autowired
	OrganizationItemAssembler orgItemAssembler;
	
	@Autowired
	OrganizationRepository orgRepo;
	
	@Autowired
	SecurityService authService;

	
	@Override
	public ResponseEntity<OrganizationList> listOrganizations(OrganizationOrdering sort, Direction sortDirection, Integer limit, Long offset, String q) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_VIEWER, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_EDITOR);
		
		// Posso avere N autorizzazioni valide con ruolo user_viewer o user_editor
		// Alcune di queste avranno organizzazioni associate, altre no.
		// Se sono admin o ce n'è una senza organizzazioni associate allora posso vedere tutte
		// Se tutte le autorizzazioni hanno delle organizzazioni definite allora posso vedere solo quelle definite.
				
		Specification<OrganizationEntity> spec;
		
		if (this.authService.canReadAllOrganizations()) {
			spec = OrganizationFilters.empty();
		} else {
			Set<Long> orgIds = this.authService.listAuthorizedOrganizations(
					GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_EDITOR, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_VIEWER);
			
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
		
		OrganizationList ret = ListaUtils.costruisciListaPaginata(organizations, pageRequest.limit, curRequest, new OrganizationList());
		for (OrganizationEntity org : organizations) {
			ret.addItemsItem(this.orgItemAssembler.toModel(org));
		}
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<Organization> readOrganization(Long id) {
		
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_VIEWER, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_EDITOR);

		Organization ret = this.orgRepo.findById(id)
			.map( org -> this.orgAssembler.toModel(org))
			.orElseThrow( () -> new ResourceNotFoundException(OrganizationMessages.notFound(id)));
		
		return ResponseEntity.ok(ret);
	}

}
