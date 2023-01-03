package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Organization;
import it.govhub.govregistry.commons.api.beans.OrganizationCreate;
import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.utils.Base64String;
import it.govhub.govregistry.readops.api.spec.OrganizationApi;

@Component
public class OrganizationAssembler extends RepresentationModelAssemblerSupport<OrganizationEntity, Organization> {

	public OrganizationAssembler() {
		super(OrganizationApi.class, Organization.class);
	}
	

	@Override
	public Organization toModel(OrganizationEntity src) {
		Organization ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		
		if (src.getLogo() != null) {
			ret.setLogo(new Base64String(src.getLogo()));
		}
		
		if (src.getLogoMiniature() != null) {
			ret.setLogoMiniature(new Base64String(src.getLogoMiniature()));
		}
		
		ret.add(linkTo(
				methodOn(OrganizationApi.class)
				.readOrganization(src.getId()))
			.withSelfRel()
		) ;
		
		
		return ret;
	}

	
	public OrganizationEntity toEntity(OrganizationCreate src) {
		OrganizationEntity ret = new OrganizationEntity();
		BeanUtils.copyProperties(src,  ret);
		
		if (src.getLogo() != null) {
			ret.setLogo(src.getLogo().getDecodedValue());
		}
		
		if (src.getLogoMiniature() != null) {
			ret.setLogoMiniature(src.getLogoMiniature().getDecodedValue());
		}
		
		return ret;
	}
	

}
