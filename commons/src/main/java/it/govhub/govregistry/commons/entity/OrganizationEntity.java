package it.govhub.govregistry.commons.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
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
@Table(name = "govhub_organizations")
public class OrganizationEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_govhub_organizations", sequenceName = "seq_govhub_organizations", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_govhub_organizations")
	private Long id;

	@EqualsAndHashCode.Include
	@Column(name = "tax_code", unique = true, nullable = false)
	private String taxCode;

	@EqualsAndHashCode.Include
	@Column(name = "legal_name", unique = true, nullable = false)
	private String legalName;

	@Column(name = "office_at")
  	private String officeAt;
	  
	@Column(name = "office_address")
	private String officeAddress;

	@Column(name = "office_address_details")
	private String officeAddressDetails;

	@Column(name = "office_zip")
	private String officeZip;

	@Column(name = "office_municipality")
	private String officeMunicipality;

	@Column(name = "office_municipality_details")
	private String officeMunicipalityDetails;

	@Column(name = "office_province")
	private String officeProvince;

	@Column(name = "office_foreign_state")
	private String officeForeignState;

	@Column(name = "office_phone_number")
	private String officePhoneNumber;

	@Column(name = "office_email_address")
	private String officeEmailAddress;

	@Column(name = "office_pec_address")
	private String officePecAddress;

	@Lob
	private byte[] logoMiniature;

	@Lob
	private byte[] logo;
	
	@Column(name = "logoMiniatureMediaType")
	private String logoMiniatureMediaType;
	
	@Column(name = "logoMediaType")
	private String logoMediaType;
	
}
