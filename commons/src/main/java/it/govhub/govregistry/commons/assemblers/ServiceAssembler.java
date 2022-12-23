package it.govhub.govregistry.commons.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import it.govhub.govregistry.commons.api.beans.Service;
import it.govhub.govregistry.commons.api.beans.ServiceCreate;
import it.govhub.govregistry.commons.api.spec.ServiceApi;
import it.govhub.govregistry.commons.entity.ServiceEntity;

@Component
public class ServiceAssembler extends RepresentationModelAssemblerSupport<ServiceEntity, Service> {

	public ServiceAssembler() {
		super(ServiceApi.class, Service.class);
	}

	@Override
	public Service toModel(ServiceEntity src) {
		Service ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		ret.setServiceName(src.getName());
		
		ret.add(linkTo(
				methodOn(ServiceApi.class)
				.readService(src.getId()))
			.withSelfRel()
		) ;
		
		return ret;
	}

	public ServiceEntity toEntity(ServiceCreate service) {
		
		return ServiceEntity.builder()
			.name(service.getServiceName())
			.description(service.getDescription())
			.build();
		
	}
	

}
