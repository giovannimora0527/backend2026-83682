package co.edu.uniminuto.veterinaria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend del centro veterinario.
 */
@SpringBootApplication
public class VeterinariaApplication {

    /**
     * Arranca la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(VeterinariaApplication.class, args);
    }
}
