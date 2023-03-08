package it.govhub.govregistry.commons.entity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
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
public class ApplicationEntity {

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
	@AttributeOverrides({
		 @AttributeOverride(
		            name = "name",
		            column = @Column( name = "logo_name" )
		        ),
		 @AttributeOverride(
		            name = "url",
		            column = @Column( name = "logo_url" )
		        ),
		 @AttributeOverride(
		            name = "type",
		            column = @Column( name = "logo_type" )
		        ),
		 @AttributeOverride(
		            name = "color",
		            column = @Column( name = "logo_color" )
		        ),
		 @AttributeOverride(
		            name = "bgColor",
		            column = @Column( name = "logo_bg_color" )
		        )
	})
	private LogoEntity logo;
	
}
