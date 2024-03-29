/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
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
package it.govhub.govregistry.api.test;


import java.util.HashSet;

import org.apache.commons.codec.binary.Base64;

import it.govhub.govregistry.commons.entity.OrganizationEntity;
import it.govhub.govregistry.commons.entity.ServiceEntity;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;

public class Costanti {
	
	// Path
	public static final String AUTHORIZATIONS_BASE_PATH_DETAIL_ID = "/v1/users/{uid}/authorizations/{aid}";
	public static final String USERS_ID_AUTHORIZATIONS_BASE_PATH = "/v1/users/{id}/authorizations";
	public static final String ORGANIZATIONS_BASE_PATH = "/v1/organizations";
	public static final String ORGANIZATIONS_BASE_PATH_DETAIL_ID = ORGANIZATIONS_BASE_PATH + "/{id}";
	public static final String ORGANIZATIONS_BASE_PATH_LOGO = ORGANIZATIONS_BASE_PATH_DETAIL_ID + "/logo";
	public static final String ORGANIZATIONS_BASE_PATH_LOGO_MINIATURE = ORGANIZATIONS_BASE_PATH_DETAIL_ID + "/logo-miniature";
	public static final String SERVICES_BASE_PATH = "/v1/services";
	public static final String SERVICES_BASE_PATH_DETAIL_ID = SERVICES_BASE_PATH + "/{id}";
	public static final String SERVICES_BASE_PATH_LOGO = SERVICES_BASE_PATH_DETAIL_ID + "/logo";
	public static final String SERVICES_BASE_PATH_LOGO_MINIATURE = SERVICES_BASE_PATH_DETAIL_ID + "/logo-miniature";
	public static final String PROFILE_BASE_PATH = "/v1/profile";
	public static final String STATUS_BASE_PATH = "/v1/status";
	public static final String USERS_BASE_PATH = "/v1/users";
	public static final String USERS_BASE_PATH_DETAIL_ID = USERS_BASE_PATH + "/{id}";

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
	
	public static final String TAX_CODE_ENTE_CREDITORE_4 = "12345678904";
	public static final String LEGALNAME_ENTE_CREDITORE_4 = "Ente Creditore 4";
	
	public static final String USERS_QUERY_PARAM_LIMIT = "limit";
	public static final String USERS_QUERY_PARAM_OFFSET = "offset";
	public static final String USERS_QUERY_PARAM_Q = "q";
	public static final String USERS_QUERY_PARAM_ENABLED = "enabled";
	public static final String USERS_QUERY_PARAM_SORT = "sort";
	public static final String USERS_QUERY_PARAM_SORT_DIRECTION = "sort_direction";
	
	public static final Integer USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE = LimitOffsetPageRequest.LIMIT_DEFAULT_VALUE;
	
	public static final String QUERY_PARAM_SORT_DIRECTION_ASC = "asc";
	public static final String QUERY_PARAM_SORT_DIRECTION_DESC = "desc";
	
	
	public static final String SERVICE_NAME_TEST = "Servizio Test";
	public static final String SERVICE_NAME_TEST_2 = "Servizio Test 2";
	public static final String SERVICE_DESCRIPTION_TEST = "Generica descrizione per un servizio di test";
	
	public static final String APPLICATION_GOVREGISTRY = "govregistry";
	
	/* USERS */
	
	public static UserEntity getUser_Snakamoto() {
		UserEntity userEntity = UserEntity.builder()
				.enabled(false)
				.fullName(Costanti.FULL_NAME_SATOSHI_NAKAMOTO)
				.principal(Costanti.PRINCIPAL_SNAKAMOTO)
				.email(Costanti.EMAIL_SNAKAMOTO)
				.authorizations(new HashSet<>())
				.build();
		return userEntity;
	}
	
	public static UserEntity getUser_Vbuterin() {
		UserEntity userEntity = UserEntity.builder()
				.enabled(false)
				.fullName(Costanti.FULL_NAME_VITALIY_BUTERIN)
				.principal(Costanti.PRINCIPAL_VBUTERIN)
				.email(Costanti.EMAIL_VBUTERIN)
				.authorizations(new HashSet<>())
				.build();
		return userEntity;
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
	
	public static OrganizationEntity getEnteCreditore4() {
		return OrganizationEntity.builder()
				.taxCode(Costanti.TAX_CODE_ENTE_CREDITORE_4)
				.legalName(Costanti.LEGALNAME_ENTE_CREDITORE_4)
				.build();
	}
	/* SERVICES */
	
	public static ServiceEntity getServizioTest() {
		return ServiceEntity.builder()
				.name(Costanti.SERVICE_NAME_TEST)
				.description(Costanti.SERVICE_DESCRIPTION_TEST)
				.build();
	}
	
	public static ServiceEntity getServizioTest2() {
		return ServiceEntity.builder()
				.name(Costanti.SERVICE_NAME_TEST_2)
				.description(Costanti.SERVICE_DESCRIPTION_TEST)
				.build();
	}
}
