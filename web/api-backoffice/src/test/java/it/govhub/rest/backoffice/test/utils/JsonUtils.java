package it.govhub.rest.backoffice.test.utils;

import java.nio.charset.Charset;

import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {
	
	//private static HalHandlerInstantiator instantiator = new HalHandlerInstantiator(new DefaultRelProvider(), null,
//	            null);
	
	private static ObjectMapper mapper = new Jackson2ObjectMapperBuilder()
			.modules(new JavaTimeModule(), new Jackson2HalModule())
	//		.ha
			.build();

	public static String asJsonString(final Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T asObject(MvcResult result, Class<T> valueType) {
		try {	
			return asObject(result.getResponse().getContentAsString(Charset.forName("UTF-8")), valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static <T> T asObject(final String json, Class<T> valueType) {
		try {
			return mapper.readValue(json, valueType);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
