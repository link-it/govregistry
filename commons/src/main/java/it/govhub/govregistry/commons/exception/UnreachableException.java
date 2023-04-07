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
package it.govhub.govregistry.commons.exception;

/**
 * Eccezione sollevata quando una certa riga di codice non dovrebbe essere mai raggiunta. 
 *
 */
public class UnreachableException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public UnreachableException() {
		super("This line should never be executed!");
	}

	public UnreachableException(String msg) {
		super(msg);
	}

	public UnreachableException(Throwable t) {
		super(t);
	}

}
