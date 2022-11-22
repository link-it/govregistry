package it.govhub.rest.backoffice.web;

import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_ORGANIZATIONS_EDITOR;
import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_ORGANIZATIONS_VIEWER;
import static it.govhub.rest.backoffice.config.SecurityConfig.RUOLO_GOVHUB_SYSADMIN;

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

import it.govhub.rest.backoffice.api.OrganizationApi;
import it.govhub.rest.backoffice.assemblers.OrganizationAssembler;
import it.govhub.rest.backoffice.assemblers.OrganizationItemAssembler;
import it.govhub.rest.backoffice.beans.Organization;
import it.govhub.rest.backoffice.beans.OrganizationCreate;
import it.govhub.rest.backoffice.beans.OrganizationList;
import it.govhub.rest.backoffice.beans.OrganizationOrdering;
import it.govhub.rest.backoffice.beans.PatchOp;
import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.messages.OrganizationMessages;
import it.govhub.rest.backoffice.repository.OrganizationFilters;
import it.govhub.rest.backoffice.repository.OrganizationRepository;
import it.govhub.rest.backoffice.services.OrganizationService;
import it.govhub.rest.backoffice.services.SecurityService;
import it.govhub.rest.backoffice.utils.LimitOffsetPageRequest;
import it.govhub.rest.backoffice.utils.ListaUtils;
import it.govhub.rest.backoffice.utils.PostgreSQLUtilities;
import it.govhub.rest.backoffice.utils.RequestUtils;

@RestController
public class OrganizationController implements OrganizationApi {
	
	@Autowired
	private OrganizationAssembler orgAssembler;
	
	@Autowired
	private OrganizationItemAssembler orgItemAssembler;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	@Autowired
	private OrganizationService orgService;
	
	@Autowired
	private SecurityService authService;

	@Override
	public ResponseEntity<Organization> createOrganization(OrganizationCreate org) {
		
		this.authService.hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_ORGANIZATIONS_EDITOR);
		
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeAddress(), "office_address");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeAddressDetails(), "office_address_details");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeAt(), "office_at");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeForeignState(), "office_foreign_state");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeMunicipality(), "office_municipality");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeMunicipalityDetails(), "office_municipality_details");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeProvince(), "office_province");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeZip(), "office_zip");
		
		OrganizationEntity created = this.orgService.createOrganization(org);
		Organization ret = this.orgAssembler.toModel(created);
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	
	@Override
	public ResponseEntity<OrganizationList> listOrganizations(OrganizationOrdering sort, Direction sortDirection, Integer limit, Long offset, String q) {
		
		this.authService.hasAnyRole(RUOLO_GOVHUB_SYSADMIN, RUOLO_GOVHUB_ORGANIZATIONS_VIEWER, RUOLO_GOVHUB_ORGANIZATIONS_EDITOR);
		
		// Posso avere N autorizzazioni valide con ruolo user_viewer o user_editor
		// Alcune di queste avranno organizzazioni associate, altre no.
		// Se sono admin o ce n'è una senza organizzazioni associate allora posso vedere tutte
		// Se tutte le autorizzazioni hanno delle organizzazioni definite allora posso vedere solo quelle definite.
				
		Specification<OrganizationEntity> spec;
		
		if (this.authService.canReadAllOrganizations()) {
			spec = OrganizationFilters.empty();
		} else {
			Set<Long> orgIds = this.authService.listAuthorizedOrganizations(
					RUOLO_GOVHUB_ORGANIZATIONS_EDITOR, RUOLO_GOVHUB_ORGANIZATIONS_VIEWER);
			
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
		
		this.authService.hasAnyOrganizationAuthority(id, RUOLO_GOVHUB_ORGANIZATIONS_VIEWER, RUOLO_GOVHUB_ORGANIZATIONS_EDITOR);

		Organization ret = this.orgRepo.findById(id)
			.map( org -> this.orgAssembler.toModel(org))
			.orElseThrow( () -> new ResourceNotFoundException(OrganizationMessages.notFound(id)));
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<Organization> updateOrganization(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyOrganizationAuthority(id, RUOLO_GOVHUB_ORGANIZATIONS_EDITOR);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		OrganizationEntity updated = this.orgService.patchOrganization(id, patch);
		Organization ret = this.orgAssembler.toModel(updated);
		
		return ResponseEntity.ok(ret);
	}


}
