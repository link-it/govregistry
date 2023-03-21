package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationCreate;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.readops.api.web.ReadOrganizationController;

@Component
public class OrganizationAssembler extends RepresentationModelAssemblerSupport<OrganizationEntity, Organization> {
	
	Logger log = LoggerFactory.getLogger(OrganizationAssembler.class);

	public OrganizationAssembler() {
		super(ReadOrganizationController.class, Organization.class);
	}
	

	@Override
	public Organization toModel(OrganizationEntity src) {
		log.debug("Assembling Entity [Organization] to model...");
		Organization ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		
		ret.add(linkTo(
				methodOn(ReadOrganizationController.class)
				.readOrganization(src.getId()))
			.withSelfRel()
		);
		
		if(src.getLogo() != null) {
			ret.add(linkTo(
					methodOn(ReadOrganizationController.class)
					.downloadOrganizationLogo(src.getId()))
				.withRel("logo"));
		}
		
		if (src.getLogoMiniature() != null) {
			ret.add(linkTo(
						methodOn(ReadOrganizationController.class)
						.downloadOrganizationLogoMiniature(src.getId()))
				.withRel("logo-miniature"));
		}
			
		return ret;
	}

	
	public OrganizationEntity toEntity(OrganizationCreate src) {
		log.debug("Converting Model [OrganizationCreate] to Entity");

		OrganizationEntity ret = new OrganizationEntity();
		BeanUtils.copyProperties(src, ret);

		return ret;
	}
	

}
