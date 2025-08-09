package br.com.desafio.celula_financeiro_controladoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

/**
 * Teste de conectividade com Oracle controlado por variável de ambiente.
 *
 * Para rodar:
 * export RUN_DB_TESTS=true
 * ./mvnw -Dtest=OracleConnectionConditionalTest test
 */
class OracleConnectionConditionalTest {

    @Test
    void deveConectarNoOracleQuandoHabilitado() throws Exception {
        boolean enabled = Boolean.parseBoolean(System.getenv().getOrDefault("RUN_DB_TESTS", "false"));
        assumeTrue(enabled, "Defina RUN_DB_TESTS=true para habilitar este teste.");

        // ATENÇÃO: mantenha os valores consistentes com o application.properties
        String url = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
        String user = "APP_FINANCEIRO";
        String pass = "APP_FINANCEIRO";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("select 1 from dual")) {

            rs.next();
            assertEquals(1, rs.getInt(1));
        }
    }
}
