package br.com.desafio.celula_financeiro_controladoria;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifica que o contexto Spring sobe sem exigir DB (útil na CI).
 */
@SpringBootTest
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
class SmokeContextLoadsTest {

    @Test
    void contextLoads() {
        // Se o contexto subir, já passa.
    }
}
