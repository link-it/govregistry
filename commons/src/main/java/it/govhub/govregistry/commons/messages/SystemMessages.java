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
package it.govhub.govregistry.commons.messages;

public class SystemMessages {

	public static String internalError() {
		return "Request can't be satisfied at the moment";
	}
	
	public static String endpointNotFound() {
		return "Resource not found."; 
	}
	
	public static String sessionExpired() {
		return "This session has been expired (possibly due to multiple concurrent logins being attempted as the same user)";
	}

	
	private SystemMessages() {
	}

}
