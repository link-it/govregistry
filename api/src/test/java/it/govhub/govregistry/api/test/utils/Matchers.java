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
package it.govhub.govregistry.api.test.utils;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;

import java.util.ArrayList;

import org.hamcrest.Matcher;
import org.hamcrest.collection.IsMapContaining;

public class Matchers {


	public static <K> Matcher<?> hasNullOrEmpty(K key) {
		return anyOf(
				not(	IsMapContaining.hasKey(key)),
				IsMapContaining.hasEntry(key, new ArrayList<>()));
	}
	
	
	private Matchers() {	}

	
}
