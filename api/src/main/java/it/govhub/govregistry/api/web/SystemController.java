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
package it.govhub.govregistry.api.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import it.govhub.govregistry.api.spec.SystemApi;
import it.govhub.govregistry.commons.api.beans.Problem;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.handlers.RestResponseEntityExceptionHandler;

@V1RestController
public class SystemController implements SystemApi {
	
	@Override
	public ResponseEntity<Problem> status() {
		Problem ret =  RestResponseEntityExceptionHandler.buildProblem(HttpStatus.OK,"System is working correctly");
		ret.removeLinks();
		
		return ResponseEntity.ok(ret);
	}

}
