package it.govhub.rest.backoffice.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.rest.backoffice.beans.OrganizationItem;
import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.web.OrganizationController;

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
