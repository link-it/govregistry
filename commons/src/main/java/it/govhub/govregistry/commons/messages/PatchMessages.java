/*
 * GovRegistry - Registries manager for GovHub
 *
 * Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
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

import org.springframework.validation.Errors;

import it.govhub.govregistry.commons.utils.RequestUtils;

public class PatchMessages {
	
	private PatchMessages() {}

	public static final String VOID_OBJECT_PATCH = "Invalid PATCH operations: result is an empty object.";
	
	public static String validationFailed(Errors errors) {
		return "Patched object violates schema: " + RequestUtils.extractValidationError(errors.getAllErrors().get(0));		
	}
}