package it.govhub.govregistry.api.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.govhub.govregistry.api.repository.UserRepository;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.ConflictException;
import it.govhub.govregistry.commons.exception.SemanticValidationException;
import it.govhub.govregistry.commons.messages.UserMessages;
import it.govhub.govregistry.readops.api.assemblers.UserAssembler;

@Service
public class UserService {
		
	@Autowired
	UserAssembler userAssembler;
	
	@Autowired
	UserMessages userMessages;
	
	@Autowired
	UserRepository userRepo;
	
	Logger log = LoggerFactory.getLogger(UserService.class);
	
	
	@Transactional
	public UserEntity createUser(UserEntity userCreate) {
		log.info("Creating new user: {}", userCreate);
		
		if (this.userRepo.findByPrincipal(userCreate.getPrincipal()).isPresent()) {
			throw new ConflictException(this.userMessages.conflictPrincipal(userCreate.getPrincipal()));
		}
		
		return this.userRepo.save(userCreate);
	}
	
	/**
	 * Sostituisce la vecchia entità con la nuova, effettuando i controlli necessari.
	 * 
	 */
	@Transactional
	public UserEntity replaceUser(UserEntity oldUser, UserEntity newUser) {
		log.info("Replacing User {} With {}", oldUser, newUser);
		if (oldUser.getId() != newUser.getId()) {
			throw new SemanticValidationException(this.userMessages.fieldNotModificable("id"));
		}
		
		// Cerco conflitti un'altro  utente con lo stesso principal ma id diverso.
		Optional<UserEntity> conflictUser = this.userRepo.findByPrincipal(newUser.getPrincipal())
				.filter( u -> !u.getId().equals(newUser.getId()));
		
		if (conflictUser.isPresent() ) {
			throw new ConflictException(this.userMessages.conflictPrincipal(newUser.getPrincipal()));
		}
				
		return this.userRepo.save(newUser);
	}
	
}
