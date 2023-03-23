package it.govhub.security.config;

public class GovregistryRoles {

	public static final String REALM_NAME = "govhub";
	public static final String GOVREGISTRY_SYSADMIN = "govhub_sysadmin"; // Accesso a tutte le risorse
	public static final String GOVREGISTRY_USERS_EDITOR = "govhub_users_editor";
	public static final String GOVREGISTRY_USERS_VIEWER = "govhub_users_viewer";
	public static final String GOVREGISTRY_USER = "govhub_user";
	public static final String GOVREGISTRY_ORGANIZATIONS_EDITOR = "govhub_organizations_editor";
	public static final String GOVREGISTRY_ORGANIZATIONS_VIEWER = "govhub_organizations_viewer";
	public static final String GOVREGISTRY_SERVICES_EDITOR = "govhub_services_editor";
	public static final String GOVREGISTRY_SERVICES_VIEWER = "govhub_services_viewer";

	private GovregistryRoles() {		}
}
