package it.govhub.govregistry.readops.api.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceCreate;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.utils.Base64String;
import it.govhub.govregistry.readops.api.spec.ServiceApi;

@Component
public class ServiceAssembler extends RepresentationModelAssemblerSupport<ServiceEntity, Service> {
	
	Logger log = LoggerFactory.getLogger(ServiceAssembler.class);

	public ServiceAssembler() {
		super(ServiceApi.class, Service.class);
	}

	@Override
	public Service toModel(ServiceEntity src) {
		log.debug("Assembling Entity [Service] to model...");
		Service ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		ret.setServiceName(src.getName());
		
		if (src.getLogo() != null) {
			ret.setLogo(new Base64String(src.getLogo()));
		}
		
		if (src.getLogoMiniature() != null) {
			ret.setLogoMiniature(new Base64String(src.getLogoMiniature()));
		}
		
		ret.add(linkTo(
				methodOn(ServiceApi.class)
				.readService(src.getId()))
			.withSelfRel()
		) ;
		
		return ret;
	}

	public ServiceEntity toEntity(ServiceCreate src) {
		log.debug("Converting Model [ServiceCreate] to entity...");
		
		var ret = ServiceEntity.builder()
			.name(src.getServiceName())
			.description(src.getDescription())
			.build();
		
		if (src.getLogo() != null) {
			ret.setLogo(src.getLogo().getDecodedValue());
		}
		
		if (src.getLogoMiniature() != null) {
			ret.setLogoMiniature(src.getLogoMiniature().getDecodedValue());
		}
		
		return ret;
	}
	

}
