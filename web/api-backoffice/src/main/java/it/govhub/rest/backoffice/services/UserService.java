package it.govhub.rest.backoffice.services;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.govhub.rest.backoffice.beans.UserCreate;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.exception.ConflictException;
import it.govhub.rest.backoffice.repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepo;

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

}
