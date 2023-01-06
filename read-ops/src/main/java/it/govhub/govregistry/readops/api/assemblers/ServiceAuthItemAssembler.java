package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.ServiceAuthItem;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.spec.ServiceApi;

@Component
public class ServiceAuthItemAssembler extends RepresentationModelAssemblerSupport<ServiceEntity, ServiceAuthItem>{

	public ServiceAuthItemAssembler() {
		super(ServiceApi.class, ServiceAuthItem.class);
	}

	
	@Override
	public ServiceAuthItem toModel(ServiceEntity src) {
		ServiceAuthItem ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		ret.setServiceName(src.getName());
		
		ret.add(linkTo(
				methodOn(ServiceApi.class)
				.readService(src.getId()))
			.withSelfRel()
		) ;
		
		ret.add(linkTo(
				methodOn(ServiceApi.class)
				.downloadServiceLogo(src.getId()))
				.withRel("logo"));
		
		ret.add(linkTo(
				methodOn(ServiceApi.class)
				.downloadServiceLogoMiniature(src.getId()))
			.withRel("logo_small")
		) ;
		
		return ret; 
	}
	

}
