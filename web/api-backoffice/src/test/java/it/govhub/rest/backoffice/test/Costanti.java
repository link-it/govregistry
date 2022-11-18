package it.govhub.rest.backoffice.test;

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
	
	public static final String USERS_QUERY_PARAM_LIMIT = "limit";
	public static final String USERS_QUERY_PARAM_OFFSET = "offset";
	public static final String USERS_QUERY_PARAM_Q = "q";
	public static final String USERS_QUERY_PARAM_ENABLED = "enabled";
	public static final String USERS_QUERY_PARAM_SORT = "sort";
	public static final String USERS_QUERY_PARAM_SORT_DIRECTION = "sort_direction";
	
	public static final Integer USERS_QUERY_PARAM_LIMIT_DEFAULT_VALUE = LimitOffsetPageRequest.LIMIT_DEFAULT_VALUE;
	
	public static final String QUERY_PARAM_SORT_DIRECTION_ASC = "asc";
	public static final String QUERY_PARAM_SORT_DIRECTION_DESC = "desc";
	
	public static UserEntity getUser_Snakamoto() {
		UserEntity userEntity = new UserEntity();
		userEntity.setEnabled(false);
		userEntity.setFullName(Costanti.FULL_NAME_SATOSHI_NAKAMOTO);
		userEntity.setPrincipal(Costanti.PRINCIPAL_SNAKAMOTO);
		userEntity.setEmail(Costanti.EMAIL_SNAKAMOTO);
		return userEntity;
	}
	
	public static UserEntity getUser_Vbuterin() {
		UserEntity userEntity = new UserEntity();
		userEntity.setEnabled(false);
		userEntity.setFullName(Costanti.FULL_NAME_VITALIY_BUTERIN);
		userEntity.setPrincipal(Costanti.PRINCIPAL_VBUTERIN);
		userEntity.setEmail(Costanti.EMAIL_VBUTERIN);
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
}
