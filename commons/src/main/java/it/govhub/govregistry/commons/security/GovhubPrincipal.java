package it.govhub.govregistry.commons.security;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import it.govhub.govregistry.commons.entity.UserEntity;

public class GovhubPrincipal implements UserDetails {

	private static final long serialVersionUID = 1L;
	
	private UserEntity user;
	
	private List<GrantedAuthority> authorities = Collections.emptyList();
	
	public GovhubPrincipal(UserEntity user) {
		this.user = user;
	}

	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return this.authorities;	// Non usiamo il meccanismo di autorizzazione di spring ma andiamo per logica di business
	}

	@Override
	public String getPassword() {
		return "{noop}password";	// TODO
	}

	@Override
	public String getUsername() {
		return user.getPrincipal();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return user.getEnabled();
	}


	public UserEntity getUser() {
		return user;
	}


	public void setUser(UserEntity user) {
		this.user = user;
	}



}
