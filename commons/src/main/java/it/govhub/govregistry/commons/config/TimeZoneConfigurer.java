package it.govhub.govregistry.commons.config;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeZoneConfigurer {
	
	@Value("${govhub.time-zone}")
	String timezone;
	
	Logger logger = LoggerFactory.getLogger(TimeZoneConfigurer.class);

	@PostConstruct
	public void setTimezone() {
		this.logger.info("Setting Application TimeZone to: " + this.timezone);
		TimeZone.setDefault(TimeZone.getTimeZone(this.timezone));
	}
	
}
