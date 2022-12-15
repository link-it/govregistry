package it.govhub.govregistry.commons.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data	
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "govhub_users")
public class UserEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name="seq_govhub_users",sequenceName="seq_govhub_users", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq_govhub_users")
	private Long id;

	@EqualsAndHashCode.Include
	@Column(name = "principal", unique=true, nullable = false)
	private String principal;

	@Column(name = "full_name", nullable = false)
	private String fullName;
	
	@Column(name = "email")
	private String email;
	
	@Column(name = "enabled", nullable=false)
	private Boolean enabled;
	
	@OneToMany(mappedBy = "user")
	private Set<RoleAuthorizationEntity> authorizations;

}