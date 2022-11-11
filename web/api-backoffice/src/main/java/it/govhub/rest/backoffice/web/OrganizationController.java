package it.govhub.rest.backoffice.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.webjars.NotFoundException;

import com.github.fge.jsonpatch.JsonPatch;

import it.govhub.rest.backoffice.api.OrganizationsApi;
import it.govhub.rest.backoffice.assemblers.OrganizationAssembler;
import it.govhub.rest.backoffice.assemblers.OrganizationItemAssembler;
import it.govhub.rest.backoffice.beans.Organization;
import it.govhub.rest.backoffice.beans.OrganizationCreate;
import it.govhub.rest.backoffice.beans.OrganizationList;
import it.govhub.rest.backoffice.beans.OrganizationOrdering;
import it.govhub.rest.backoffice.beans.PatchOp;
import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.messages.OrganizationMessages;
import it.govhub.rest.backoffice.repository.OrganizationFilters;
import it.govhub.rest.backoffice.repository.OrganizationRepository;
import it.govhub.rest.backoffice.services.OrganizationService;
import it.govhub.rest.backoffice.utils.LimitOffsetPageRequest;
import it.govhub.rest.backoffice.utils.ListaUtils;
import it.govhub.rest.backoffice.utils.RequestUtils;

@RestController
public class OrganizationController implements OrganizationsApi {
	
	@Autowired
	private OrganizationAssembler orgAssembler;
	
	@Autowired
	private OrganizationItemAssembler orgItemAssembler;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	@Autowired
	private OrganizationService orgService;

	@Override
	public ResponseEntity<Organization> createOrganization(OrganizationCreate org) {
		
		OrganizationEntity created = this.orgService.createOrganization(org);
		Organization ret = this.orgAssembler.toModel(created);
		return ResponseEntity.status(HttpStatus.CREATED).body(ret);
	}

	
	@Override
	public ResponseEntity<OrganizationList> listOrganizations(Integer limit, Long offset, String q, OrganizationOrdering sort) {
		
		Specification<OrganizationEntity> spec = OrganizationFilters.empty();
		if (q != null) {
			spec = OrganizationFilters.likeTaxCode(q).or(OrganizationFilters.likeLegalName(q));
		}
		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit, OrganizationFilters.sort(sort));
		
		Page<OrganizationEntity> organizations = this.orgRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		OrganizationList ret = ListaUtils.costruisciListaPaginata(organizations, curRequest, new OrganizationList());
		for (OrganizationEntity org : organizations) {
			ret.addItemsItem(this.orgItemAssembler.toModel(org));
		}
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<Organization> readOrganization(Long id) {
		
		Organization ret = this.orgRepo.findById(id)
			.map( org -> this.orgAssembler.toModel(org))
			.orElseThrow( () -> new NotFoundException(OrganizationMessages.notFound(id)));
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<Organization> updateOrganization(Long id, List<PatchOp> patchOp) {
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
		OrganizationEntity updated = this.orgService.patchOrganization(id, patch);
		Organization ret = this.orgAssembler.toModel(updated);
		
		return ResponseEntity.ok(ret);
	}


}
