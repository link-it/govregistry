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
package it.govhub.govregistry.commons.security;

import java.io.IOException;
import java.net.URI;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.api.beans.Problem;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.exception.handlers.RestResponseEntityExceptionHandler;

/*
* Handler che viene eseguito quando un oggetto {@link org.springframework.security.core.Authentication Authentication} non ha
* l'autorizzazione richiesta. (e.g.: visistare un'endpoint) 
*/

public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

	@Autowired
	private ObjectMapper jsonMapper;
	
	Logger logger = LoggerFactory.getLogger(AccessDeniedHandlerImpl.class);
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
		logger.debug("Building problem for Access Denied: {}", accessDeniedException.getMessage());
		
		Problem problem = new Problem();
		
		problem.setStatus(HttpStatus.FORBIDDEN.value());
		problem.setTitle(HttpStatus.FORBIDDEN.getReasonPhrase());
		problem.setDetail(accessDeniedException.getMessage());
		
		// imposto il content-type della risposta
		response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setStatus(problem.getStatus());
		
		ServletOutputStream outputStream = null;
		try{
			problem.setInstance(new URI(RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.FORBIDDEN)));
			outputStream = response.getOutputStream();
			this.jsonMapper.writeValue(outputStream, problem);
			outputStream.flush();
		}catch(Exception e) {
			throw new UnreachableException();
		}
	}

}
