package it.govhub.rest.backoffice.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.beans.BeanUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

import it.govhub.rest.backoffice.beans.ServiceAuthItem;
import it.govhub.rest.backoffice.entity.ServiceEntity;
import it.govhub.rest.backoffice.web.ServiceController;

public class ServiceAuthItemAssembler extends RepresentationModelAssemblerSupport<ServiceEntity, ServiceAuthItem>{

	public ServiceAuthItemAssembler() {
		super(ServiceController.class, ServiceAuthItem.class);
	}

	
	@Override
	public ServiceAuthItem toModel(ServiceEntity src) {
		ServiceAuthItem ret = instantiateModel(src);
		
		BeanUtils.copyProperties(src, ret);
		ret.setServiceName(src.getName());
		
		ret.add(linkTo(
				methodOn(ServiceController.class)
				.readService(src.getId()))
			.withSelfRel()
		) ;
		
		return ret; 
	}
	

}
