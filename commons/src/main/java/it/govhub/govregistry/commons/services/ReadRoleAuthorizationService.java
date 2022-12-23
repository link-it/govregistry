package it.govhub.govregistry.commons.services;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import it.govhub.govregistry.commons.api.beans.AuthorizationList;
import it.govhub.govregistry.commons.assemblers.AuthorizationAssembler;
import it.govhub.govregistry.commons.entity.RoleAuthorizationEntity;
import it.govhub.govregistry.commons.repository.RoleAuthorizationFilters;
import it.govhub.govregistry.commons.repository.RoleAuthorizationRepository;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;

@Service
public class ReadRoleAuthorizationService {
	
	@Autowired
	private RoleAuthorizationRepository authRepo;
	
	@Autowired
	private AuthorizationAssembler authAssembler;

	@Transactional
	public AuthorizationList listUserAuthorizations(Long id, LimitOffsetPageRequest pageRequest) {
		
		Specification<RoleAuthorizationEntity> spec = RoleAuthorizationFilters.byUser(id);
		
		Page<RoleAuthorizationEntity> auths = this.authRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		AuthorizationList ret = ListaUtils.costruisciListaPaginata(auths,  pageRequest.limit, curRequest, new AuthorizationList());
		
		for (RoleAuthorizationEntity auth : auths) {
			ret.addItemsItem(this.authAssembler.toModel(auth));
		}
		return ret;
	}
}
