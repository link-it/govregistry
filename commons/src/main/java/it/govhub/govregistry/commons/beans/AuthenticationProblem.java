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
package it.govhub.govregistry.commons.beans;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Questa classe è identica a it.govhub.govregistry.api.beans.Problem eccetto per il fatto che non 
 * eredita da RepresentationModel.
 * E' necessaria perchè se nel 'commence' di un AuthenticationEntrypoint restituiamo il Problem di sopra,
 * ci troviamo un array vuoto chiamato "links".
 * 
 *
 */
public class AuthenticationProblem {

	@JsonProperty("detail")
	public String detail;

	@JsonProperty("instance")
	public URI instance;

	@JsonProperty("status")
	public Integer status;

	@JsonProperty("title")
	public String title;

	@JsonProperty("type")
	public URI type = URI.create("about:blank");

}
