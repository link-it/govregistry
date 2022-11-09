package it.govhub.rest.backoffice.entity;

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
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "organizations")
public class OrganizationEntity {

	@Id
	@SequenceGenerator(name = "seq_organizations", sequenceName = "seq_organizations", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_organizations")
	private Long id;

	@Column(name = "tax_code", unique = true, nullable = false, length = 11)
	private String taxCode;

	@Column(name = "legal_name", unique = true, nullable = false, length = 80)
	private String legalName;

	@Column(name = "office_at", length = 120)
  	private String officeAt;
	  
	@Column(name = "office_address", length = 120)
	private String officeAddress;

	@Column(name = "office_address_details", length = 120)
	private String officeAddressDetails;

	@Column(name = "office_zip", length = 120)
	private String officeZip;

	@Column(name = "office_municipality", length = 120)
	private String officeMunicipality;

	@Column(name = "office_municipality_details", length = 120)
	private String officeMunicipalityDetails;

	@Column(name = "office_province", length = 120)
	private String officeProvince;

	@Column(name = "office_foreign_state", length = 120)
	private String officeForeignState;

	@Column(name = "office_phone_number", length = 120)
	private String officePhoneNumber;

	@Column(name = "office_email_address", length = 120)
	private String officeEmailAddress;

	@Column(name = "office_pec_address", length = 120)
	private String officePecAddress;

	@Lob
	private byte[] logoMiniature;

	@Lob
	private byte[] logo;
}
