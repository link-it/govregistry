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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import it.govhub.govregistry.commons.api.beans.PageInfo;
import it.govhub.govregistry.commons.exception.UnreachableException;


public class ListaUtils {
	
	private ListaUtils() {	}
	
	public static <T> List<T> emptyIfNull(List<T> iterable) {
	    return iterable == null ? Collections.<T>emptyList() : iterable;
	}
	
	/** 
	 * Costruisce una lista paginata riempiendola con i riferimenti a 
	 * first, prev, next e last.
	 * 
	 * Mentre il limit non viene toccato.
	 * 
	 */
	public static final <T extends RepresentationModel<T>> T buildPaginatedList(
			Page<?> results,
			int limit,
			HttpServletRequest request, 
			T destList)  {
		
		long startOffset = results.getNumber() * (long) limit;

		if (!results.isFirst()) {
			String firstLink = replaceOffset(request, 0);
			destList.add(Link.of(firstLink, "first"));
		}
		
		if (results.hasPrevious()) {
			long prevOffset = startOffset - limit;
			String previousLink = replaceOffset(request, prevOffset);
			destList.add(Link.of(previousLink, "prev"));
		}
		
		if (results.hasNext()) {
			long newOffset = startOffset + limit;
			String nextLink = replaceOffset(request, newOffset);
			destList.add(Link.of(nextLink, "next"));
		}
		
		if (!results.isLast()) {
			long endOffset = (results.getTotalPages()-1) * (long) limit;
			String lastLink = replaceOffset(request, endOffset);
			destList.add(Link.of(lastLink,"last"));
		}
		
		PageInfo pageInfo = new PageInfo();
		pageInfo.setLimit(limit);
		pageInfo.setOffset(startOffset);
		pageInfo.setTotal(results.getTotalElements());
		set(destList, "Page", pageInfo);
		
		return destList;
	}

	
	private static void set(Object obj, String field, Object value) {
		try {
			Method method = Class.forName(obj.getClass().getName()).getMethod("set"+field, value.getClass());
			method.invoke(obj, value);
		} catch(Exception e) {
			throw new UnreachableException("Check how you called me");
		}
	}
	
	
	/**
	 * Rimpiazza limit e offset nella URL di richiesta e restituisce la nuova URL
	 * 
	 */
	private static String replaceOffset(HttpServletRequest request, long offset) {
		
		
		UriBuilder builder = new DefaultUriBuilderFactory().builder()
				.scheme(request.getScheme())
				.host(request.getServerName())
				.port(request.getServerPort())
				.path(request.getRequestURI())
				.queryParam("offset", offset);
		
		
		for(Entry<String, String[]> p : request.getParameterMap().entrySet()) {
			String name = p.getKey();
			if (!name.equalsIgnoreCase("offset")) {
				builder.queryParam(name, (Object[])p.getValue());
			}
		}
		return builder.build().toString();
	}
	
	
}
