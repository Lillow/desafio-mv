package br.com.desafio.celula_financeiro_controladoria.domain.infra;

import java.math.BigDecimal;
import java.sql.Types;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OracleFinanceiroDao {

    private final JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall spRegistrarMov;

    @PostConstruct
    void init() {
        // chama PKG_FINANCEIRO.REGISTRAR_MOVIMENTO
        this.spRegistrarMov = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FINANCEIRO")
                .withProcedureName("REGISTRAR_MOVIMENTO")
                .declareParameters(
                        new SqlOutParameter("p_mov_id", Types.NUMERIC));
    }

    public Long registrarMovimento(Long contaId, String tipo, BigDecimal valor, String origem, String descricao) {
        var params = new java.util.HashMap<String, Object>();
        params.put("p_conta_id", contaId);
        params.put("p_tipo", tipo); // "CREDITO" ou "DEBITO"
        params.put("p_valor", valor);
        params.put("p_origem", origem);
        params.put("p_descricao", descricao);

        var out = spRegistrarMov.execute(params);
        var id = ((Number) out.get("p_mov_id")).longValue();
        return id;
    }

    private SimpleJdbcCall fnCalcTaxa;

    @PostConstruct
    void init2() {
        this.fnCalcTaxa = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_FINANCEIRO")
                .withFunctionName("CALC_TAXA_PERIODO")
                .declareParameters(
                        new SqlParameter("p_cliente_id", Types.NUMERIC),
                        new SqlParameter("p_data_ini", Types.TIMESTAMP),
                        new SqlParameter("p_data_fim", Types.TIMESTAMP));
    }

    public BigDecimal calcularTaxaPeriodo(Long clienteId, java.time.LocalDate inicio, java.time.LocalDate fim) {
        var ini = java.sql.Timestamp.valueOf(inicio.atStartOfDay());
        var end = java.sql.Timestamp.valueOf(fim.atTime(23, 59, 59));
        Number n = fnCalcTaxa.executeFunction(Number.class, clienteId, ini, end);
        return (n == null) ? BigDecimal.ZERO : new BigDecimal(n.toString()).setScale(2);
    }

}
