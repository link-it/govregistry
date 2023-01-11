package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.OrganizationItem;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.readops.api.spec.OrganizationApi;



@Component
public class OrganizationItemAssembler extends RepresentationModelAssemblerSupport<OrganizationEntity, OrganizationItem> {
	
	public OrganizationItemAssembler() {
		super(OrganizationApi.class, OrganizationItem.class);
	}
	
	@Override
	public OrganizationItem toModel(OrganizationEntity src) {
		OrganizationItem ret = instantiateModel(src);
		BeanUtils.copyProperties(src, ret);
		
		ret.add(linkTo(
				methodOn(OrganizationApi.class)
				.readOrganization(src.getId()))
			.withSelfRel()
		) ;
		
		ret.add(linkTo(
				methodOn(OrganizationApi.class)
				.downloadOrganizationLogo(src.getId()))
				.withRel("logo"));
		
		ret.add(linkTo(
				methodOn(OrganizationApi.class)
				.downloadOrganizationLogoMiniature(src.getId()))
			.withRel("logo_small")
		) ;
		
		return ret;
	}



}
