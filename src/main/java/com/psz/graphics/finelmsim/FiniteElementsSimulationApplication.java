package com.psz.graphics.finelmsim;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FiniteElementsSimulationApplication {

		public static void main(String[] args) {
			new SpringApplicationBuilder(FiniteElementsSimulationApplication.class)
			.headless(false)
			.web(WebApplicationType.NONE)
			.run(args);
	}

}
