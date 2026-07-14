package com.caique.advancedcrud;

import org.springframework.boot.SpringApplication;

public class TestAdvancedCrudApplication {

	public static void main(String[] args) {
		SpringApplication.from(AdvancedCrudApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
