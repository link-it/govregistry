package it.govhub.rest.backoffice.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import it.govhub.rest.backoffice.assemblers.UserAssembler;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.BadRequestException;
import it.govhub.rest.backoffice.exception.ConflictException;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.repository.UserRepository;
import it.govhub.rest.backoffice.utils.PostgreSQLUtilities;
import it.govhub.rest.backoffice.utils.RequestUtils;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private org.springframework.validation.Validator validator;
	
	@Autowired
	private UserAssembler userAssembler;
	

	@Transactional
	public UserEntity createUser(UserCreate userCreate) {
		
		Optional<UserEntity> found = this.userRepo.findByPrincipal(userCreate.getPrincipal());
		if (found.isPresent()) {
			throw new ConflictException("User with principal ["+userCreate.getPrincipal()+"] already exists");
		}
		
		UserEntity newUser = UserEntity.builder()
				.principal(userCreate.getPrincipal())
				.full_name(userCreate.getFullName())
				.enabled(userCreate.isEnabled())
				.email(userCreate.getEmail())
				.build();
		
		return this.userRepo.save(newUser);
	}
	
	
	@Transactional
	public UserEntity patchUser(Long id, JsonPatch patch) {
		
		UserEntity user = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException("User with ID ["+id+"] not found"));
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		User restUser = this.userAssembler.toModel(user);
		JsonNode jsonUser = this.objectMapper.convertValue(restUser, JsonNode.class);
		
		JsonNode newJsonUser;
		try {
			newJsonUser = patch.apply(jsonUser);
		} catch (JsonPatchException e) {			
			throw new BadRequestException(e.getLocalizedMessage());
		}
		
		// Lo converto nell'oggetto userCreate
		UserCreate updatedContact;
		try {
			updatedContact = this.objectMapper.treeToValue(newJsonUser, UserCreate.class);
		} catch (JsonProcessingException e) {
			throw new BadRequestException(e);
		}
		
		if (updatedContact == null) {
			throw new BadRequestException("PATCH non valida, risulterebbe in un oggetto nullo.");
		}
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedContact, updatedContact.getClass().getName());
		validator.validate(updatedContact, errors);
		if (errors != null && !errors.getAllErrors().isEmpty()) {
			String msg = "L'oggetto patchato viola i vincoli dello schema: " + RequestUtils.extractValidationError(errors.getAllErrors().get(0));
			throw new BadRequestException(msg);
		}
		
		// Faccio partire la validazione custom per la stringa \u0000
		PostgreSQLUtilities.throwIfContainsNullByte(updatedContact.getFullName(), "full_name");
		
		// Dall'oggetto REST passo alla entity
		UserEntity newUser = UserEntity.builder()
				.email(updatedContact.getEmail())
				.enabled(updatedContact.isEnabled())
				.full_name(updatedContact.getFullName())
				.id(id)
				.principal(updatedContact.getPrincipal())
				.build();
		
		newUser = this.userRepo.save(newUser);

		return newUser;
		
	}

}
