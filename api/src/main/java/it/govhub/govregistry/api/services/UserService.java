/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
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
	
	@Transactional
	public UserEntity replaceUser(UserEntity oldUser, UserEntity newUser) {
		log.info("Replacing User {} With {}", oldUser, newUser);
		if ( ! oldUser.getId().equals(newUser.getId())) {
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
