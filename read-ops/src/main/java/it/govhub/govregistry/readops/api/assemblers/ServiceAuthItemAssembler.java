package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.ServiceAuthItem;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.readops.api.spec.ServiceApi;

@Component
public class ServiceAuthItemAssembler extends RepresentationModelAssemblerSupport<ServiceEntity, ServiceAuthItem>{
	
	Logger log = LoggerFactory.getLogger(ServiceAuthItemAssembler.class);

	public ServiceAuthItemAssembler() {
		super(ServiceApi.class, ServiceAuthItem.class);
	}

	
	@Override
	public ServiceAuthItem toModel(ServiceEntity src) {
		log.debug("Assembling Entity [Service] to model...");
		ServiceAuthItem ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		ret.setServiceName(src.getName());
		
		ret.add(linkTo(
				methodOn(ServiceApi.class)
				.readService(src.getId()))
			.withSelfRel()
		) ;
		
		if (src.getLogo() != null) {
			ret.add(linkTo(
					methodOn(ServiceApi.class)
					.downloadServiceLogo(src.getId()))
					.withRel("logo"));
		}
		
		if (src.getLogoMiniature() != null) { 
			ret.add(linkTo(
					methodOn(ServiceApi.class)
					.downloadServiceLogoMiniature(src.getId()))
				.withRel("logo-miniature")
			) ;
		}
		
		return ret; 
	}	

}
