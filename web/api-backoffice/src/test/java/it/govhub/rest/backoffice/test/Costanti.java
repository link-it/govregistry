package it.govhub.rest.backoffice.test;

import it.govhub.rest.backoffice.entity.OrganizationEntity;
import it.govhub.rest.backoffice.entity.UserEntity;
import it.govhub.rest.backoffice.security.GovhubPrincipal;
import it.govhub.rest.backoffice.utils.LimitOffsetPageRequest;

public class Costanti {

	public static final String STRING_256 = "abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234abcd1234";

	public static final String WRONG_EMAIL = "aa aaa@as.rs";
	
	public static final String EMAIL_SNAKAMOTO = "snakamoto@mail.xx";
	public static final String PRINCIPAL_SNAKAMOTO = "snakamoto";
	public static final String FULL_NAME_SATOSHI_NAKAMOTO = "Satoshi Nakamoto";
	
	public static final String FULL_NAME_VITALIY_BUTERIN = "Vitaliy Buterin";
	public static final String PRINCIPAL_VBUTERIN = "vbuterin";
	public static final String EMAIL_VBUTERIN = "vbuterin@mail.xx";
	
	public static final String TAX_CODE_ENTE_CREDITORE_3 = "12345678903";
	public static final String LEGALNAME_ENTE_CREDITORE_3 = "Ente Creditore 3";
	
	public static final String USERS_QUERY_PARAM_LIMIT = "limit";
	public static final String USERS_QUERY_PARAM_OFFSET = "offset";
	public static final String USERS_QUERY_PARAM_Q = "q";
	public static final String USERS_QUERY_PARAM_ENABLED = "enabled";
	public static final String USERS_QUERY_PARAM_SORT = "sort";
	public static final String USERS_QUERY_PARAM_SORT_DIRECTION = "sort_direction";
	
	public static final Integer USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE = LimitOffsetPageRequest.LIMIT_DEFAULT_VALUE;
	
	public static final String QUERY_PARAM_SORT_DIRECTION_ASC = "asc";
	public static final String QUERY_PARAM_SORT_DIRECTION_DESC = "desc";
	
	/* USERS */
	
	public static UserEntity getUser_Snakamoto() {
		UserEntity userEntity = UserEntity.builder()
				.enabled(false)
				.fullName(Costanti.FULL_NAME_SATOSHI_NAKAMOTO)
				.principal(Costanti.PRINCIPAL_SNAKAMOTO)
				.email(Costanti.EMAIL_SNAKAMOTO)
				.build();
		return userEntity;
	}
	
	public static UserEntity getUser_Vbuterin() {
		UserEntity userEntity = UserEntity.builder()
				.enabled(false)
				.fullName(Costanti.FULL_NAME_VITALIY_BUTERIN)
				.principal(Costanti.PRINCIPAL_VBUTERIN)
				.email(Costanti.EMAIL_VBUTERIN)
				.build();
		return userEntity;
	}
	
	public static GovhubPrincipal getPrincipal_Admin() {
		UserEntity userEntity = UserEntity.builder()
				.enabled(true)
				.email(Costanti.EMAIL_SNAKAMOTO)
				.principal("amministratore")
				.id(1L)
				.build();
		
		return new GovhubPrincipal(userEntity);
				
	}
	
	/* ORGANIZATIONS */
	
	public static OrganizationEntity getEnteCreditore3() {
		return OrganizationEntity.builder()
				.taxCode(Costanti.TAX_CODE_ENTE_CREDITORE_3)
				.legalName(Costanti.LEGALNAME_ENTE_CREDITORE_3)
				.officeAddress("Piazza Italia 1")
				.officeAddressDetails("Piazza Italia 1")
				.officeAt("Interno 1")
				.officeEmailAddress("entecreditore3@email.com")
				.officeForeignState("Italy")
				.officeMunicipality("Roma")
				.officeMunicipalityDetails("Municipio 3")
				.officePecAddress("entecreditore3@pec.it")
				.officePhoneNumber("00 1234 5678")
				.officeProvince("RO")
				.officeZip("0000")
				.build();
	}
}
