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
import it.govhub.govregistry.readops.api.web.ReadServiceController;

@Component
public class ServiceAssembler extends RepresentationModelAssemblerSupport<ServiceEntity, Service> {
	
	Logger log = LoggerFactory.getLogger(ServiceAssembler.class);

	public ServiceAssembler() {
		super(ReadServiceController.class, Service.class);
	}

	@Override
	public Service toModel(ServiceEntity src) {
		log.debug("Assembling Entity [Service] to model...");
		Service ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		ret.setServiceName(src.getName());
		
		ret.add(linkTo(
				methodOn(ReadServiceController.class)
				.readService(src.getId()))
			.withSelfRel()
		);
		
		if (src.getLogo() != null) {
			ret.add(linkTo(
					methodOn(ReadServiceController.class)
					.downloadServiceLogo(src.getId()))
				.withRel("logo")
			);
		}
		
		if (src.getLogoMiniature() != null) {
			ret.add(linkTo(
					methodOn(ReadServiceController.class)
					.downloadServiceLogoMiniature(src.getId()))
				.withRel("logo-miniiature")
			);
		}
		
		return ret;
	}

	public ServiceEntity toEntity(ServiceCreate src) {
		log.debug("Converting Model [ServiceCreate] to entity...");
		
		var ret = ServiceEntity.builder()
			.name(src.getServiceName())
			.description(src.getDescription())
			.build();
		
		return ret;
	}
	

}
