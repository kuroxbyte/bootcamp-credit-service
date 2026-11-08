package dev.bootcamp.creditservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de credit-service.
 */
@SpringBootApplication
public class CreditServiceApplication {

    /**
     * Arranca el servicio.
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        SpringApplication.run(CreditServiceApplication.class, args);
    }
}
