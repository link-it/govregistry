package it.govhub.rest.backoffice.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import it.govhub.rest.backoffice.api.UsersApi;
import it.govhub.rest.backoffice.assemblers.LimitOffsetPageRequest;
import it.govhub.rest.backoffice.assemblers.UserAssembler;
import it.govhub.rest.backoffice.beans.PatchOp;
import it.govhub.rest.backoffice.beans.User;
import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.beans.UserList;
import it.govhub.rest.backoffice.beans.UserOrdering;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.BadRequestException;
import it.govhub.rest.backoffice.exception.ResourceNotFoundException;
import it.govhub.rest.backoffice.repository.UserFilters;
import it.govhub.rest.backoffice.repository.UserRepository;
import it.govhub.rest.backoffice.services.UserService;
import it.govhub.rest.backoffice.utils.ListaUtils;
import it.govhub.rest.backoffice.utils.PostgreSQLUtilities;
import it.govhub.rest.backoffice.utils.RequestUtils;


@RestController
public class UsersController implements UsersApi {
	

	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private UserAssembler userAssembler;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private org.springframework.validation.Validator validator;

	@Override
	public ResponseEntity<User> readUser(Long id) {
		
		UserEntity user = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException("User with ID ["+id+"] not found"));
		
		User ret = this.userAssembler.toModel(user);
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<UserList> listUsers(Integer limit, Long offset, String q, Boolean enabled, UserOrdering orderBy) {

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit);
		
		Specification<UserEntity> spec = UserFilters.empty();
		if (q != null) {
			spec = UserFilters.likeFullName(q).or(UserFilters.likePrincipal(q));
		}
		if (enabled != null) {
			spec = spec.and(UserFilters.byEnabled(enabled));
		}
		
		Page<UserEntity> users = this.userRepo.findAll(spec, pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		UserList ret = ListaUtils.costruisciListaPaginata(users, curRequest, new UserList());
		for (UserEntity user : users) {
			ret.addItemsItem(this.userAssembler.toModel(user));
		}
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<User> updateUser(Long id, List<PatchOp> patchOp) {
		
		UserEntity user = this.userRepo.findById(id)
				.orElseThrow( () -> new ResourceNotFoundException("User with ID ["+id+"] not found"));
		
		// Otteniamo l'oggetto JsonPatch
		JsonPatch patch = RequestUtils.toJsonPatch(patchOp);
		
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

		// Dalla entity ripasso al model
		User ret = this.userAssembler.toModel(newUser);
		
		return ResponseEntity.ok(ret);
	}

	
	@Override
	public ResponseEntity<User> createUser(UserCreate userCreate) {
		UserEntity newUser = this.userService.createUser(userCreate);

		User ret = this.userAssembler.toModel(newUser);
		return ResponseEntity.ok(ret);
	}

}
