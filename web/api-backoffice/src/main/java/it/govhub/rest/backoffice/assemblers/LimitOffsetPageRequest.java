package it.govhub.rest.backoffice.assemblers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


/**
 * Le specifiche AGID richiedono di utilizzare offset o cursor per lo scostamento nella paginazione, JPA usa invece
 * il numero di pagina. Questo wrapper per un pageable consente di lavorare con JPA e poi  produrre risultati
 * REST validi per AGID, perchè conserva il valore dell'offset e limit.
 * 
 *
 */
public class LimitOffsetPageRequest 
{
	public final Pageable pageable;
	public final long offset;
	public final int limit;
	
	public LimitOffsetPageRequest(Long offset, Integer limit)
	{
		this(offset, limit, Sort.unsorted());
	}
	
	public LimitOffsetPageRequest(Long offset, Integer limit, Sort sort)
	{
		// default per limit a 25
		if (limit == null ) {
			limit = 25;
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
	
	private Integer getNumeroPagina()
	{		
		return Long.valueOf(Math.floorDiv(this.offset, this.limit)).intValue();
	}
}

 