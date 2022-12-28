package it.govhub.govregistry.api.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.govregistry.api.services.OrganizationService;
import it.govhub.govregistry.api.spec.OrganizationApi;
import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationCreate;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.utils.PostgreSQLUtilities;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.govregistry.readops.api.assemblers.OrganizationAssembler;
import it.govhub.security.config.GovregistryRoles;
import it.govhub.security.services.SecurityService;

@RestController
public class OrganizationController implements OrganizationApi {
	
	@Autowired
	OrganizationAssembler orgAssembler;
	
	@Autowired
	OrganizationService orgService;
	
	@Autowired
	SecurityService authService;

	@Override
	public ResponseEntity<Organization> createOrganization(OrganizationCreate org) {
		
		this.authService.expectAnyRole(GovregistryRoles.RUOLO_GOVHUB_SYSADMIN, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_EDITOR);
		
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
		
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.RUOLO_GOVREGISTRY_ORGANIZATIONS_EDITOR);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		OrganizationEntity updated = this.orgService.patchOrganization(id, patch);
		Organization ret = this.orgAssembler.toModel(updated);
		
		return ResponseEntity.ok(ret);
	}

	
}
