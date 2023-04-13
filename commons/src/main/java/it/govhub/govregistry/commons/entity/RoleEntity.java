/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "govhub_roles")
public class RoleEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_govhub_roles", sequenceName = "seq_govhub_roles", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govhub_roles")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_govhub_application", nullable=false, foreignKey = @ForeignKey(name = "GovhubRole_GovhubApplication"))
	private ApplicationEntity govhubApplication;

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
			inverseJoinColumns = @JoinColumn(name = "assignable_role_id" ),
			foreignKey = @ForeignKey(name="GovhubAssRole_GovhubRole"), 
			inverseForeignKey = @ForeignKey(name="GovhubAssRole_AssignedGovhubRole"))
	private Set<RoleEntity> assignableRoles;

}
