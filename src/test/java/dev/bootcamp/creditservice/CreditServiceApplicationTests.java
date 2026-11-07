package dev.bootcamp.creditservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Se habilita cuando existan los adaptadores de los puertos (MongoDB y clientes HTTP)")
@SpringBootTest
class CreditServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
