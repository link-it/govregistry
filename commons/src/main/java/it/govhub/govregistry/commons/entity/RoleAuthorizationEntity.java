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
package it.govhub.govregistry.commons.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Una autorizzazione concede ad un utente un ruolo per dei servizii ed organizzazioni.   
 *
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "govhub_authorizations")
public class RoleAuthorizationEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@EqualsAndHashCode.Include
	@Id
	@SequenceGenerator(name = "seq_govhub_authorizations", sequenceName = "seq_govhub_authorizations", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govhub_authorizations")
	private Long id;
	
	
	@ManyToOne
    @JoinColumn(name = "id_govhub_user", nullable=false, foreignKey = @ForeignKey(name = "GovhubAuth_GovhubUser"))
	private UserEntity user;
	
	
	@ManyToOne
    @JoinColumn(name = "id_govhub_role", nullable=false, foreignKey = @ForeignKey(name = "GovhubAuth_GovhubRole"))
	private RoleEntity role;
	
	
	@Column(name = "expiration_date")
	private OffsetDateTime expirationDate;
	
	
	@ManyToMany
	@JoinTable(
			name = "govhub_auth_services",
			joinColumns = @JoinColumn(name = "id_govhub_authorization"),
			inverseJoinColumns = @JoinColumn(name = "id_govhub_service"), 
			foreignKey = @ForeignKey(name = "GovhubAuthService_GovhubAuth"),
			inverseForeignKey = @ForeignKey(name = "GovhubAuthOrganization_GovhubService"))
	private Set<ServiceEntity> services;
	
	
	@ManyToMany
	@JoinTable(
			name = "govhub_auth_organizations",
			joinColumns = @JoinColumn(name = "id_govhub_authorization"),
			inverseJoinColumns = @JoinColumn(name = "id_govhub_organization"),
			foreignKey = @ForeignKey(name = "GovhubAuthOrganization_GovhubAuth"),
			inverseForeignKey = @ForeignKey(name = "GovhubAuthOrganization_GovhubOrganization"))
	private Set<OrganizationEntity> organizations;
	
}
