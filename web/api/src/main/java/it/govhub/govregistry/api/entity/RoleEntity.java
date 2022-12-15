package it.govhub.govregistry.api.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "govhub_roles")
public class RoleEntity {

	@Id
	@SequenceGenerator(name = "seq_govhub_roles", sequenceName = "seq_govhub_roles", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govhub_roles")
	private Long id;
	
	@Column(name = "id_govhub_application", nullable = false)
	private Long idGovhubApplication;

	@Column(name = "name", unique = true, nullable = false)
	@EqualsAndHashCode.Include				// Siccome RoleEntity viene usato all'interno di collezioni definiamo il suo hashCode e equals
	private String name;
	
	
	@Column(name = "description")
	private String description;
	
	
	// Recuperiamo i ruoli in maniera lazy per evitare lunghe catene di ricorsioni
	
	@ManyToMany
	@JoinTable(
			name = "govhub_assignable_roles", 
			joinColumns = @JoinColumn(name = "role_id"),
			inverseJoinColumns = @JoinColumn(name = "assignable_role_id" ))
	private Set<RoleEntity> assignableRoles;
	
}
