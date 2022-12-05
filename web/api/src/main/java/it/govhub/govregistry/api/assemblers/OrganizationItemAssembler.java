package it.govhub.govregistry.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.api.beans.OrganizationItem;
import it.govhub.govregistry.api.entity.OrganizationEntity;
import it.govhub.govregistry.api.web.OrganizationController;

@Component
public class OrganizationItemAssembler extends RepresentationModelAssemblerSupport<OrganizationEntity, OrganizationItem> {
	
	public OrganizationItemAssembler() {
		super(OrganizationController.class, OrganizationItem.class);
	}
	
	@Override
	public OrganizationItem toModel(OrganizationEntity src) {
		OrganizationItem ret = instantiateModel(src);
		BeanUtils.copyProperties(src, ret);
		
		ret.add(linkTo(
				methodOn(OrganizationController.class)
				.readOrganization(src.getId()))
			.withSelfRel()
		) ;
		
		return ret;
	}



}
