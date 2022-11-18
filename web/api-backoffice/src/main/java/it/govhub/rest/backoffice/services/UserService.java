package it.govhub.rest.backoffice.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import it.govhub.rest.backoffice.assemblers.UserAssembler;
import it.govhub.rest.backoffice.beans.Profile;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.BadRequestException;
import it.govhub.rest.backoffice.exception.ConflictException;
import it.govhub.rest.backoffice.exception.ForbiddenException;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.messages.PatchMessages;
import it.govhub.rest.backoffice.messages.SecurityMessages;
import it.govhub.rest.backoffice.messages.UserMessages;
import it.govhub.rest.backoffice.repository.UserRepository;
import it.govhub.rest.backoffice.utils.PostgreSQLUtilities;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private Validator validator;
	
	@Autowired
	private UserAssembler userAssembler;
	

	@Transactional
	public UserEntity createUser(UserCreate userCreate) {
		
		if (this.userRepo.findByPrincipal(userCreate.getPrincipal()).isPresent()) {
			throw new ConflictException(UserMessages.conflictPrincipal(userCreate.getPrincipal()));
		}
		
		UserEntity newUser = this.userAssembler.toEntity(userCreate);
				
		return this.userRepo.save(newUser);
	}
	
	
	@Transactional
	public UserEntity patchUser(Long id, JsonPatch patch) {
		
		UserEntity user = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException(UserMessages.notFound(id)));
		
		// Convertiamo la entity in json e applichiamo la patch sul json
		User restUser = this.userAssembler.toModel(user);
		JsonNode jsonUser = this.objectMapper.convertValue(restUser, JsonNode.class);
		
		JsonNode newJsonUser;
		try {
			newJsonUser = patch.apply(jsonUser);
		} catch (JsonPatchException e) {			
			throw new BadRequestException(e.getLocalizedMessage());
		}
		
		// Lo converto nell'oggetto User, sostituendo l'ID per essere sicuri che la patch
		// non l'abbia cambiato.
		User updatedContact;
		try {
			updatedContact = this.objectMapper.treeToValue(newJsonUser, User.class);
		} catch (JsonProcessingException e) {
			throw new BadRequestException(e);
		}
		
		if (updatedContact == null) {
			throw new BadRequestException(PatchMessages.voidObjectPatch);
		}
		updatedContact.setId(id);
		
		// Faccio partire la validazione
		Errors errors = new BeanPropertyBindingResult(updatedContact, updatedContact.getClass().getName());
		validator.validate(updatedContact, errors);
		if (!errors.getAllErrors().isEmpty()) {
			throw new BadRequestException(PatchMessages.validationFailed(errors));
		}
		
		// Faccio partire la validazione custom per la stringa \u0000
		PostgreSQLUtilities.throwIfContainsNullByte(updatedContact.getFullName(), "full_name");
		
		// Dall'oggetto REST passo alla entity
		UserEntity newUser = this.userAssembler.toEntity(updatedContact);

		// Controllo eventuali conflitti sugli indici
		Optional<UserEntity> conflictUser = this.userRepo.findByPrincipal(newUser.getPrincipal())
				.filter( u -> !u.getId().equals(newUser.getId()));
		
		if (conflictUser.isPresent() ) {
			throw new ConflictException(UserMessages.conflictPrincipal(newUser.getPrincipal()));
		}
				
		return this.userRepo.save(newUser);
	}
	
	
	@Transactional
	public Profile getProfile(String principal) {
		UserEntity user = this.userRepo.findByPrincipal(principal)
				.orElseThrow( () -> new ForbiddenException(SecurityMessages.authorizedUserNotInDb(principal)));
		
		return this.userAssembler.toProfileModel(user);
	}

}
