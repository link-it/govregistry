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
package it.govhub.govregistry.commons.exception;

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
