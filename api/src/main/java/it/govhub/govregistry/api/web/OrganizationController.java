package it.govhub.govregistry.api.web;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import it.govhub.govregistry.api.repository.OrganizationRepository;
import it.govhub.govregistry.api.services.OrganizationService;
import it.govhub.govregistry.api.spec.OrganizationApi;
import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationCreate;
import it.govhub.govregistry.commons.api.beans.OrganizationList;
import it.govhub.govregistry.commons.api.beans.OrganizationOrdering;
import it.govhub.govregistry.commons.api.beans.PatchOp;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.messages.OrganizationMessages;
import it.govhub.govregistry.commons.messages.PatchMessages;
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
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	Validator validator;
	
	@Autowired
	OrganizationMessages orgMessages;
	
	@Autowired
	OrganizationRepository writeOrgRepo;
	
	Logger log = LoggerFactory.getLogger(OrganizationController.class);
	
	
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

		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeAddress(), "office_address");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeAddressDetails(), "office_address_details");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeAt(), "office_at");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeForeignState(), "office_foreign_state");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeMunicipality(), "office_municipality");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeMunicipalityDetails(), "office_municipality_details");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeProvince(), "office_province");
		PostgreSQLUtilities.throwIfContainsNullByte(org.getOfficeZip(), "office_zip");
		
		this.authService.expectAnyRole(GovregistryRoles.GOVREGISTRY_SYSADMIN, GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR);
		
		OrganizationEntity newOrg = this.orgAssembler.toEntity(org);
		newOrg = this.orgService.createOrganization(newOrg);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(this.orgAssembler.toModel(newOrg));
	}

	
	
	@Override
	public ResponseEntity<Organization> updateOrganization(Long id, List<PatchOp> patchOp) {
		
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		OrganizationEntity org = this.orgRepo.findById(id)
			.orElseThrow( () -> new ResourceNotFoundException(this.orgMessages.idNotFound(id)));
		
		log.info("Patching organization [{}]: {}", id,  patch);
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		Organization restOrg = this.orgAssembler.toModel(org);
		JsonNode newJsonOrg;
		try {
			JsonNode jsonOrg = this.objectMapper.convertValue(restOrg, JsonNode.class);
			newJsonOrg= patch.apply(jsonOrg);
		} catch (JsonPatchException e) {			
			throw new BadRequestException(e.getLocalizedMessage());
		}
		
		// Lo converto nell'oggetto OrganizationCreate, sostituendo l'ID per essere sicuri che la patch
		// non l'abbia cambiato.
		OrganizationCreate updatedOrganization;
		try {
			updatedOrganization = this.objectMapper.treeToValue(newJsonOrg, OrganizationCreate.class);
		} catch (JsonProcessingException e) {
			throw new BadRequestException(e);
		}
		
		if (updatedOrganization == null) {
			throw new BadRequestException(PatchMessages.VOID_OBJECT_PATCH);
		}
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedOrganization, updatedOrganization.getClass().getName());
		validator.validate(updatedOrganization, errors);
		if (!errors.getAllErrors().isEmpty()) {
			throw new BadRequestException(PatchMessages.validationFailed(errors));
		}
		
		// Faccio partire la validazione custom per la stringa \u0000
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeAddress(), "office_address");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeAddressDetails(), "office_address_details");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeAt(), "office_at");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeForeignState(), "office_foreign_state");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeMunicipality(), "office_municipality");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeMunicipalityDetails(), "office_municipality_details");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeProvince(), "office_province");
		PostgreSQLUtilities.throwIfContainsNullByte(updatedOrganization.getOfficeZip(), "office_zip");
		
		
		// Dall'oggetto REST passo alla entity
		OrganizationEntity newOrg = this.orgAssembler.toEntity(updatedOrganization);
		newOrg.setId(id);
		
		newOrg = this.orgService.replaceOrganization(org, newOrg);
		
		return ResponseEntity.ok(this.orgAssembler.toModel(newOrg));
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


	@Override
	public ResponseEntity<Void> removeOrganizationLogo(Long id) {
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.orgMessages.idNotFound(id)));
		
		org.setLogo(null);
		org.setLogoMediaType(null);
		this.writeOrgRepo.save(org);
		return ResponseEntity.ok().build();
	}


	@Override
	public ResponseEntity<Void> removeOrganizationLogoMiniature(Long id) {
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.orgMessages.idNotFound(id)));
		
		org.setLogoMiniature(null);
		org.setLogoMiniatureMediaType(null);
		this.writeOrgRepo.save(org);
		return ResponseEntity.ok().build();
	}


	@Override
	public ResponseEntity<Void> updateOrganizationLogo(Long id, Resource body) {
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.orgMessages.idNotFound(id)));
		
		try {
			HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
					.currentRequestAttributes()).getRequest();
			
			org.setLogo(body.getInputStream().readAllBytes());
			org.setLogoMediaType(curRequest.getContentType());
		} catch (IOException e) {
			throw new InternalException(e);
		}
		
		this.writeOrgRepo.save(org);

		return ResponseEntity.ok().build();
	}


	@Override
	public ResponseEntity<Void> updateOrganizationLogoMiniature(Long id,	Resource body) {
		this.authService.hasAnyOrganizationAuthority(id, GovregistryRoles.GOVREGISTRY_ORGANIZATIONS_EDITOR, GovregistryRoles.GOVREGISTRY_SYSADMIN);
		
		OrganizationEntity org = this.orgRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(this.orgMessages.idNotFound(id)));
		
		try {
			HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
					.currentRequestAttributes()).getRequest();
			
			org.setLogoMiniature(body.getInputStream().readAllBytes());
			org.setLogoMiniatureMediaType(curRequest.getContentType());
		} catch (IOException e) {
			throw new InternalException(e);
		}
		
		this.writeOrgRepo.save(org);

		return ResponseEntity.ok().build();
	}

	
}
