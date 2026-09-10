package com.glowuprizz.lead_crm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LeadCrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(LeadCrmApplication.class, args);
	}

}
