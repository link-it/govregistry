package it.govhub.rest.backoffice.test;


import org.apache.commons.codec.binary.Base64;

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
	public static final byte[] LOGO_ENTE_CREDITORE_3 = "iVBORw0KGgoAAAANSUhEUgAAABgAAAAYCAYAAADgdz34AAAABHNCSVQICAgIfAhkiAAAAAlwSFlzAAAApgAAAKYB3X3/OAAAABl0RVh0U29mdHdhcmUAd3d3Lmlua3NjYXBlLm9yZ5vuPBoAAANCSURBVEiJtZZPbBtFFMZ/M7ubXdtdb1xSFyeilBapySVU8h8OoFaooFSqiihIVIpQBKci6KEg9Q6H9kovIHoCIVQJJCKE1ENFjnAgcaSGC6rEnxBwA04Tx43t2FnvDAfjkNibxgHxnWb2e/u992bee7tCa00YFsffekFY+nUzFtjW0LrvjRXrCDIAaPLlW0nHL0SsZtVoaF98mLrx3pdhOqLtYPHChahZcYYO7KvPFxvRl5XPp1sN3adWiD1ZAqD6XYK1b/dvE5IWryTt2udLFedwc1+9kLp+vbbpoDh+6TklxBeAi9TL0taeWpdmZzQDry0AcO+jQ12RyohqqoYoo8RDwJrU+qXkjWtfi8Xxt58BdQuwQs9qC/afLwCw8tnQbqYAPsgxE1S6F3EAIXux2oQFKm0ihMsOF71dHYx+f3NND68ghCu1YIoePPQN1pGRABkJ6Bus96CutRZMydTl+TvuiRW1m3n0eDl0vRPcEysqdXn+jsQPsrHMquGeXEaY4Yk4wxWcY5V/9scqOMOVUFthatyTy8QyqwZ+kDURKoMWxNKr2EeqVKcTNOajqKoBgOE28U4tdQl5p5bwCw7BWquaZSzAPlwjlithJtp3pTImSqQRrb2Z8PHGigD4RZuNX6JYj6wj7O4TFLbCO/Mn/m8R+h6rYSUb3ekokRY6f/YukArN979jcW+V/S8g0eT/N3VN3kTqWbQ428m9/8k0P/1aIhF36PccEl6EhOcAUCrXKZXXWS3XKd2vc/TRBG9O5ELC17MmWubD2nKhUKZa26Ba2+D3P+4/MNCFwg59oWVeYhkzgN/JDR8deKBoD7Y+ljEjGZ0sosXVTvbc6RHirr2reNy1OXd6pJsQ+gqjk8VWFYmHrwBzW/n+uMPFiRwHB2I7ih8ciHFxIkd/3Omk5tCDV1t+2nNu5sxxpDFNx+huNhVT3/zMDz8usXC3ddaHBj1GHj/As08fwTS7Kt1HBTmyN29vdwAw+/wbwLVOJ3uAD1wi/dUH7Qei66PfyuRj4Ik9is+hglfbkbfR3cnZm7chlUWLdwmprtCohX4HUtlOcQjLYCu+fzGJH2QRKvP3UNz8bWk1qMxjGTOMThZ3kvgLI5AzFfo379UAAAAASUVORK5CYII=".getBytes();
	public static final byte[] LOGO_MINIATURA_ENTE_CREDITORE_3 = LOGO_ENTE_CREDITORE_3;
	
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
				.logo(Base64.decodeBase64(LOGO_ENTE_CREDITORE_3))
				.logoMiniature(Base64.decodeBase64(LOGO_MINIATURA_ENTE_CREDITORE_3))
				.build();
	}
}
