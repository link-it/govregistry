package it.govhub.rest.backoffice.entity;

import java.time.OffsetDateTime;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
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
public class RoleAuthorizationEntity {

	@EqualsAndHashCode.Include
	@Id
	@SequenceGenerator(name = "seq_govhub_authorizations", sequenceName = "seq_govhub_authorizations", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govhub_authorizations")
	private Long id;
	
	
	@ManyToOne
    @JoinColumn(name = "id_govhub_user")
	private UserEntity user;
	
	
	@ManyToOne
    @JoinColumn(name = "id_govhub_role")
	private RoleEntity role;
	
	
	@Column(name = "expiration_date")
	private OffsetDateTime expirationDate;
	
	
	@ManyToMany
	@JoinTable(
			name = "govhub_auth_services",
			joinColumns = @JoinColumn(name = "id_govhub_authorization"),
			inverseJoinColumns = @JoinColumn(name = "id_govhub_service"))
	private Set<ServiceEntity> services;
	
	
	@ManyToMany
	@JoinTable(
			name = "govhub_auth_organizations",
			joinColumns = @JoinColumn(name = "id_govhub_authorization"),
			inverseJoinColumns = @JoinColumn(name = "id_govhub_organization"))
	private Set<OrganizationEntity> organizations;
	
}
