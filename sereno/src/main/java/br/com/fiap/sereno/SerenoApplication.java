package br.com.fiap.sereno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SerenoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SerenoApplication.class, args);
	}

}
