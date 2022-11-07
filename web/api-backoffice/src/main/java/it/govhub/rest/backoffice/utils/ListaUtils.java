package it.govhub.rest.backoffice.utils;

import java.lang.reflect.Method;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;


public class ListaUtils {
	
	/** 
	 * Costruisce una lista paginata riempiendola con i riferimenti a 
	 * first, prev, next e last.
	 * 
	 * Mentre il limit non viene toccato.
	 * 
	 */
	public static final <T> T costruisciListaPaginata(
			Page<?> results, 
			HttpServletRequest request, 
			T destList)  {
		
		int limit = results.getNumberOfElements();
		long startOffset = results.getNumber() * limit;

		if (!results.isFirst()) {
			String previousLink = replaceOffset(request, 0);
			set(destList, "First", previousLink);
		}
		
		if (results.hasPrevious()) {
			long prevOffset = startOffset - limit;
			String previousLink = replaceOffset(request, prevOffset);
			set(destList, "Prev", previousLink);
		}
		
		if (results.hasNext()) {
			long newOffset = startOffset + limit;
			String nextLink = replaceOffset(request, newOffset);
			set(destList, "Next", nextLink);
		}
		
		if (!results.isLast()) {
			long endOffset = (results.getTotalPages()-1) * limit;
			String lastLink = replaceOffset(request, endOffset);
			set(destList, "Last", lastLink);
		}
		
		set(destList, "Total", results.getTotalElements());
		
		return destList;
		
	}

	
	private static void set(Object obj, String field, Object value) {
		try {
			Method method = Class.forName(obj.getClass().getName()).getMethod("set"+field, value.getClass());
			method.invoke(obj, value);
		} catch(Exception e) {}
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
			
			if (name.equalsIgnoreCase("offset")) {
				continue;
			} else {							
					builder.queryParam(name, (Object[])p.getValue());
			}
		}
		
		return builder.build().toString();
	}
	
	
}
