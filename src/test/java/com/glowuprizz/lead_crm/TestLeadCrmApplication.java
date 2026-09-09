package com.glowuprizz.lead_crm;

import org.springframework.boot.SpringApplication;

public class TestLeadCrmApplication {

	public static void main(String[] args) {
		SpringApplication.from(LeadCrmApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
