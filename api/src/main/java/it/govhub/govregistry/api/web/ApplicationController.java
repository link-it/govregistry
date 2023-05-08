package it.govhub.govregistry.api.web;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.api.beans.Application;
import it.govhub.govregistry.api.beans.ApplicationList;
import it.govhub.govregistry.api.repository.ApplicationRepository;
import it.govhub.govregistry.api.spec.ApplicationApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.ApplicationEntity;
import it.govhub.govregistry.commons.exception.InternalConfigurationException;

@V1RestController
public class ApplicationController implements ApplicationApi {
	
	@Autowired
	ApplicationRepository applicationRepo;
	
	@Autowired
	ObjectMapper objectMapper;

	@Override
	public ResponseEntity<ApplicationList> listApplications() {
		
		List<ApplicationEntity> applications = this.applicationRepo.findAll();
		
		ApplicationList ret = new ApplicationList();
		
		for (var app: applications) {
			Application item = new Application();
			
			try {
				if (app.getLogo() != null) {
					JsonNode logo  = this.objectMapper.readTree(app.getLogo());
					item.setLogo(logo);
				}
			} catch (JsonProcessingException e) {
				throw new InternalConfigurationException(e.getMessage());
			}
			
			item.setApplicationId(app.getApplicationId());
			item.setApplicationName(app.getName());
			item.setDeployedUri(URI.create(app.getDeployedUri()));
			ret.addItemsItem(item);
		}
		
		return ResponseEntity.ok(ret);
	}

}
