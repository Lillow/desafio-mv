--------------------------------------------------------------------------------
-- Trigger (leve) para preencher DATA_HORA quando vier nula
--------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_MOVIMENTO_BI
BEFORE INSERT ON MOVIMENTO
FOR EACH ROW
BEGIN
  IF :NEW.DATA_HORA IS NULL THEN
    :NEW.DATA_HORA := SYSTIMESTAMP;
  END IF;
END;
/

--------------------------------------------------------------------------------
-- Pacote de operações financeiras
--------------------------------------------------------------------------------
CREATE OR REPLACE PACKAGE PKG_FINANCEIRO AS

  -- Registra uma movimentação e ajusta saldo da conta.
  -- p_tipo: 'CREDITO' ou 'DEBITO'
  PROCEDURE REGISTRAR_MOVIMENTO(
    p_conta_id   IN  NUMBER,
    p_tipo       IN  VARCHAR2,
    p_valor      IN  NUMBER,
    p_origem     IN  VARCHAR2,
    p_descricao  IN  VARCHAR2,
    p_mov_id     OUT NUMBER
  );

  -- Calcula a taxa/receita da XPTO para um cliente em um período,
  -- usando a regra de janelas de 30 dias a partir do created_at do cliente.
  FUNCTION CALC_TAXA_PERIODO(
    p_cliente_id IN NUMBER,
    p_data_ini   IN DATE,
    p_data_fim   IN DATE
  ) RETURN NUMBER;

END PKG_FINANCEIRO;
/
SHOW ERRORS

CREATE OR REPLACE PACKAGE BODY PKG_FINANCEIRO AS

  PROCEDURE REGISTRAR_MOVIMENTO(
    p_conta_id   IN  NUMBER,
    p_tipo       IN  VARCHAR2,
    p_valor      IN  NUMBER,
    p_origem     IN  VARCHAR2,
    p_descricao  IN  VARCHAR2,
    p_mov_id     OUT NUMBER
  ) IS
    v_saldo_atual  NUMBER(20,2);
    v_tipo         VARCHAR2(10) := UPPER(TRIM(p_tipo));
  BEGIN
    IF p_valor IS NULL OR p_valor <= 0 THEN
      RAISE_APPLICATION_ERROR(-20001, 'Valor deve ser positivo.');
    END IF;

    -- Valida conta / lê saldo
    SELECT SALDO
      INTO v_saldo_atual
      FROM CONTA
     WHERE ID = p_conta_id
       FOR UPDATE; -- bloqueio p/ ajuste de saldo

    IF v_tipo NOT IN ('CREDITO','DEBITO') THEN
      RAISE_APPLICATION_ERROR(-20002, 'Tipo inválido. Use CREDITO ou DEBITO.');
    END IF;

    IF v_tipo = 'DEBITO' AND (v_saldo_atual - p_valor) < 0 THEN
      RAISE_APPLICATION_ERROR(-20003, 'Saldo insuficiente para débito.');
    END IF;

    -- Insere a movimentação
    INSERT INTO MOVIMENTO (
      ATIVO, CREATED_AT, UPDATED_AT,
      DATA_HORA, DESCRICAO, ORIGEM, TIPO, VALOR, CONTA_ID
    ) VALUES (
      1, SYSTIMESTAMP, SYSTIMESTAMP,
      SYSTIMESTAMP, p_descricao, p_origem, v_tipo, p_valor, p_conta_id
    )
    RETURNING ID INTO p_mov_id;

    -- Atualiza saldo
    IF v_tipo = 'CREDITO' THEN
      UPDATE CONTA
         SET SALDO = SALDO + p_valor,
             UPDATED_AT = SYSTIMESTAMP
       WHERE ID = p_conta_id;
    ELSE
      UPDATE CONTA
         SET SALDO = SALDO - p_valor,
             UPDATED_AT = SYSTIMESTAMP
       WHERE ID = p_conta_id;
    END IF;
  END REGISTRAR_MOVIMENTO;

  FUNCTION CALC_TAXA_PERIODO(
    p_cliente_id IN NUMBER,
    p_data_ini   IN DATE,
    p_data_fim   IN DATE
  ) RETURN NUMBER IS
    v_created_at     TIMESTAMP WITH TIME ZONE;
    v_total          NUMBER(20,2) := 0;
  BEGIN
    -- Base da janela (created_at do cliente)
    SELECT CREATED_AT INTO v_created_at
      FROM CLIENTE
     WHERE ID = p_cliente_id;

    /* Contagem por janela de 30 dias:
       janela = FLOOR( (TRUNC(m.data_hora) - TRUNC(v_created_at)) / 30 )
       Só conta movimentos no período solicitado.
    */
    FOR r IN (
      SELECT janela, COUNT(*) AS qtd
        FROM (
              SELECT FLOOR((TRUNC(m.DATA_HORA) - TRUNC(CAST(v_created_at AT LOCAL TIME ZONE 'UTC' AS DATE))) / 30) AS janela
                FROM MOVIMENTO m
                JOIN CONTA c ON c.ID = m.CONTA_ID
               WHERE c.CLIENTE_ID = p_cliente_id
                 AND m.DATA_HORA BETWEEN p_data_ini AND p_data_fim
             )
       GROUP BY janela
    ) LOOP
      IF r.qtd <= 10 THEN
        v_total := v_total + (r.qtd * 1.00);
      ELSIF r.qtd <= 20 THEN
        v_total := v_total + (r.qtd * 0.75);
      ELSE
        v_total := v_total + (r.qtd * 0.50);
      END IF;
    END LOOP;

    RETURN ROUND(v_total, 2);
  END CALC_TAXA_PERIODO;

END PKG_FINANCEIRO;
/
SHOW ERRORS