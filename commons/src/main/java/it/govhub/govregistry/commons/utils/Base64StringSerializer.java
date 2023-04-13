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
package it.govhub.govregistry.commons.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class Base64StringSerializer extends StdSerializer<Base64String> {
	    
	private static final long serialVersionUID = 1L;

		public Base64StringSerializer() {
	        this(null);
	    }
	  
	    public Base64StringSerializer(Class<Base64String> t) {
	        super(t);
	    }

		@Override
	    public void serialize(
	    		Base64String value, JsonGenerator jgen, SerializerProvider provider) 
	      throws IOException {
			jgen.writeString(value.getValue());
	    }

}
