package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.OrganizationAuthItem;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.readops.api.web.ReadOrganizationController;


@Component
public class OrganizationAuthItemAssembler extends RepresentationModelAssemblerSupport<OrganizationEntity, OrganizationAuthItem> {
	
	Logger log = LoggerFactory.getLogger(OrganizationAuthItemAssembler.class);
	
	public OrganizationAuthItemAssembler() {
		super(ReadOrganizationController.class, OrganizationAuthItem.class);
	}
	
	@Override
	public OrganizationAuthItem toModel(OrganizationEntity src) {
		log.debug("Assembling Entity [Organization] into model...");
		
		OrganizationAuthItem ret = instantiateModel(src);
		BeanUtils.copyProperties(src, ret);
		
		ret.add(linkTo(
				methodOn(ReadOrganizationController.class)
				.readOrganization(src.getId()))
			.withSelfRel()
		) ;
		
		if(src.getLogo() != null)
			ret.add(linkTo(
					methodOn(ReadOrganizationController.class)
					.downloadOrganizationLogo(src.getId()))
					.withRel("logo"));
	
		if(src.getLogoMiniature() != null)
			ret.add(linkTo(
					methodOn(ReadOrganizationController.class)
					.downloadOrganizationLogoMiniature(src.getId()))
				.withRel("logo_small")
			) ;
		
		return ret;
	}


}
