package it.govhub.security.config;

import java.util.Set;

public class GovregistryRoles {

	public static final String REALM_NAME = "govhub";
	public static final String RUOLO_GOVHUB_SYSADMIN = "govhub_sysadmin"; // Accesso a tutte le risorse
	public static final String RUOLO_GOVHUB_USERS_EDITOR = "govhub_users_editor";
	public static final String RUOLO_GOVHUB_USERS_VIEWER = "govhub_users_viewer";
	public static final String RUOLO_GOVHUB_USER = "govhub_user";
	public static final String RUOLO_GOVHUB_ORGANIZATIONS_EDITOR = "govhub_organizations_editor";
	public static final String RUOLO_GOVHUB_ORGANIZATIONS_VIEWER = "govhub_organizations_viewer";
	public static final String RUOLO_GOVHUB_SERVICES_EDITOR = "govhub_services_editor";
	public static final String RUOLO_GOVHUB_SERVICES_VIEWER = "govhub_services_viewer";

	
	// impostarli nel componente jee utilizzando la funzione mappableAuthorities al posto di mappableRoles che aggiunge il prefisso 'ROLE_' ad ogni ruolo
	public static final Set<String> ruoliConsentiti = Set.of
			( 
				RUOLO_GOVHUB_SYSADMIN,
				RUOLO_GOVHUB_USERS_EDITOR,
				RUOLO_GOVHUB_USERS_VIEWER,
				RUOLO_GOVHUB_USER,
				RUOLO_GOVHUB_ORGANIZATIONS_EDITOR,
				RUOLO_GOVHUB_ORGANIZATIONS_VIEWER,
				RUOLO_GOVHUB_SERVICES_EDITOR,
				RUOLO_GOVHUB_SERVICES_VIEWER
			);

	
	private GovregistryRoles() {		}

}
