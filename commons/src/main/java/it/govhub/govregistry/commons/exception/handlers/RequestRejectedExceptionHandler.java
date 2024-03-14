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
package it.govhub.govregistry.commons.exception.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.security.web.firewall.RequestRejectedHandler;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.govhub.govregistry.commons.api.beans.Problem;

/**
 * Questo Bean Restituisce un Problem quando spring-security rifiuta una richiesta perchè ritenuta ad esempio non sicura.
 */

public class RequestRejectedExceptionHandler implements RequestRejectedHandler {
	
	Logger logger = LoggerFactory.getLogger(RequestRejectedExceptionHandler.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, RequestRejectedException ex) throws IOException, ServletException {
		logger.debug("Request was rejected:: {}", ex.getLocalizedMessage());
		
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setStatus(HttpStatus.BAD_REQUEST.value());
		
		Problem p = RestResponseEntityExceptionHandler.buildProblem(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage());
		
		ObjectMapper jm = new ObjectMapper();
		jm.setSerializationInclusion(Include.NON_NULL);
		response.getOutputStream().write(jm.writeValueAsBytes(p));
	}

}
