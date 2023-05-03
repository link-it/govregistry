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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.api.beans.Problem;
import it.govhub.govregistry.commons.exception.UnreachableException;
import it.govhub.govregistry.commons.exception.handlers.RestResponseEntityExceptionHandler;

@Component
public class UnauthorizedBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
	
	private ObjectMapper jsonMapper;
	
	Logger log = LoggerFactory.getLogger(UnauthorizedBasicAuthenticationEntryPoint.class);

	public UnauthorizedBasicAuthenticationEntryPoint(ObjectMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

    @Override
    public void commence(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        
    	response.addHeader("WWW-Authenticate", "Basic realm=\"" + getRealmName() + "\"");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        log.debug("Mappo la AuthenticationException in un problem: {}", authException.getMessage());
		
		Problem problem = new Problem();
		problem.setStatus(HttpStatus.UNAUTHORIZED.value());
		problem.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhrase());
		problem.setDetail(authException.getMessage());
		
		// imposto il content-type della risposta
		response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		response.setStatus(problem.getStatus());
		
		ServletOutputStream outputStream = null;
		try{
			problem.setInstance(new URI(RestResponseEntityExceptionHandler.problemTypes.get(HttpStatus.UNAUTHORIZED)));
			outputStream = response.getOutputStream();
			this.jsonMapper.writeValue(outputStream, problem);
			outputStream.flush();
		} catch(Exception e) {
			throw new UnreachableException(e);
		}
    }

    @Override
    public void afterPropertiesSet() {
    	// TODO
        setRealmName("Govio");
        super.afterPropertiesSet();
    }
}