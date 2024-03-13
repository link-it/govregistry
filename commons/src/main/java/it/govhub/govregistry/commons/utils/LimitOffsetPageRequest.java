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
package it.govhub.govregistry.commons.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


public class LimitOffsetPageRequest 
{
	public final Pageable pageable;
	public final long offset;
	public final int limit;
	
	public static final Integer LIMIT_DEFAULT_VALUE = 25;
	
	public LimitOffsetPageRequest(Long offset, Integer limit, Sort sort)
	{
		// default per limit a 25
		if (limit == null ) {
			limit = LIMIT_DEFAULT_VALUE;
		}
		
		if (limit <= 0) {
			throw new IllegalArgumentException("Limit must be > 0");
		}
		
		offset = (offset == null || offset < 0) ? 0 : offset;
		
		// BUGFIX: java.lang.IllegalArgumentException: first-result value cannot be negative : -2147483646
		// JPA tenterà di convertire la pagina in un offset, questa volta però convertendolo in un intero
		//e producendo un valore negativo per esso. Quindi limitiamo la grandezza dell'offset a INTMAX
		this.offset = Math.min(offset, Integer.MAX_VALUE);
		
		this.limit  = Math.min(limit, 1000);
		
		this.pageable = PageRequest.of(getNumeroPagina(), this.limit, sort);
	}
	
	private int getNumeroPagina()
	{		
		return (int) Math.floorDiv(this.offset, this.limit);
	}
}

 