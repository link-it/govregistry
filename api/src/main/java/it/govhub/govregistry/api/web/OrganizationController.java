package it.govhub.govregistry.api.web;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.govregistry.api.services.OrganizationService;
import it.govhub.govregistry.api.spec.OrganizationApi;
import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationCreate;
import it.govhub.govregistry.commons.api.beans.OrganizationList;
import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAssembler;
import it.govhub.govregistry.readops.api.assemblers.OrganizationItemAssembler;
import it.govhub.govregistry.readops.api.repository.OrganizationFilters;
import it.govhub.govregistry.readops.api.repository.ReadOrganizationRepository;
import it.govhub.govregistry.readops.api.web.ReadOrganizationController;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@V1RestController
public class OrganizationController  extends ReadOrganizationController implements OrganizationApi {
	
	@Autowired
	OrganizationAssembler orgAssembler;
	
	@Autowired
	OrganizationService orgService;
	
	@Autowired
	SecurityService authService;
	
	@Autowired
	OrganizationItemAssembler orgItemAssembler;
	
	@Autowired
	ReadOrganizationRepository orgRepo;
	
	
	private static Set<String> readOrganizationRoles = Set.of(
			GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR,
			GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_VIEWER,
			GovregistryRoles.GOVREGISTRY_SYSADMIN);

	
	@Override
	protected Set<String> getReadOrganizationRoles() {
		return new HashSet<>(readOrganizationRoles);
	}
	

	@Override
	public ResponseEntity<Organization> createOrganization(OrganizationCreate org) {
		
		this.authService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR);
		
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
	public ResponseEntity<Organization> updateOrganization(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		OrganizationEntity updated = this.orgService.patchOrganization(id, patch);
		Organization ret = this.orgAssembler.toModel(updated);
		
		return ResponseEntity.ok(ret);
	}
	
	
	@Override
	public ResponseEntity<OrganizationList> listOrganizations(
			OrganizationOrdering sort,
			Direction sortDirection, 
			Integer limit,
			Long offset,
			String q, 
			List<String> withRoles) {
		
		Set<String> roles = getReadOrganizationRoles();
		
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

	
}
