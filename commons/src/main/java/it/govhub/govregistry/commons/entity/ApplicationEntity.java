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

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "govhub_applications")
public class ApplicationEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_govhub_applications", sequenceName = "seq_govhub_applications", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govhub_applications")
	private Long id;

	@EqualsAndHashCode.Include
	@Column(name = "application_id", unique=true, nullable = false)
	private String applicationId;
	
	@Column(name = "name",  nullable = false)
	private String name;
	
	@Column(name = "deployed_uri", nullable = false)
	private String deployedUri;
	
	@Embedded
	 @AttributeOverride(
	            name = "name",
	            column = @Column( name = "logo_name" )
	        )
	 @AttributeOverride(
	            name = "url",
	            column = @Column( name = "logo_url" )
	        )
	 @AttributeOverride(
	            name = "type",
	            column = @Column( name = "logo_type" )
	        )
	 @AttributeOverride(
	            name = "color",
	            column = @Column( name = "logo_color" )
	        )
	 @AttributeOverride(
	            name = "bgColor",
	            column = @Column( name = "logo_bg_color" )
	        )
	private LogoEntity logo;
	
}