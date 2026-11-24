package com.uniminuto.clinica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de la API de la clínica veterinaria (Spring Boot). */
@SpringBootApplication
public class ClinicaApplication {

	/**
	 * Inicia la aplicación Spring Boot.
	 *
	 * @param args argumentos de línea de comandos.
	 */
	public static void main(String[] args) {
		SpringApplication.run(ClinicaApplication.class, args);
	}

}
