package com.caique.AdvancedCrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
public class AdvancedCrudApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdvancedCrudApplication.class, args);
	}

}
