package it.govhub.govregistry.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.api.beans.OrganizationAuthItem;
import it.govhub.govregistry.api.entity.OrganizationEntity;
import it.govhub.govregistry.api.web.OrganizationController;

@Component
public class OrganizationAuthItemAssembler  extends RepresentationModelAssemblerSupport<OrganizationEntity, OrganizationAuthItem> {
	
	public OrganizationAuthItemAssembler() {
		super(OrganizationController.class, OrganizationAuthItem.class);
	}
	
	@Override
	public OrganizationAuthItem toModel(OrganizationEntity src) {
		OrganizationAuthItem ret = instantiateModel(src);
		BeanUtils.copyProperties(src, ret);
		
		ret.add(linkTo(
				methodOn(OrganizationController.class)
				.readOrganization(src.getId()))
			.withSelfRel()
		) ;
		
		return ret;
	}



}
