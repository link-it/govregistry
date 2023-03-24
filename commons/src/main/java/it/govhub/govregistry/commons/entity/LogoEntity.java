package it.govhub.govregistry.commons.entity;

import java.io.Serializable;
import java.net.URL;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

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
@Embeddable
public class LogoEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Enumerated(EnumType.STRING)
	@Column(name="type", nullable=false)
	private LogoType type;

	@Column(name="url")
	private URL url;
	
	// Utilizzati per type == Svg/Bootstrap/Material
	
	@Column(name="color")
	private String color;
	
	@Column(name="bg_color")
	private String bgColor;
	
	// Utilizzato per type == Bootstrap/Maetrial
	
	@Column(name="name")
	private String name;
	
	public enum LogoType {
		IMAGE, SVG, BOOTSTRAP, MATERIAL
	}

}

