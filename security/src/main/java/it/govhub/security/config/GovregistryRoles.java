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
