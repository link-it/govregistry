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
