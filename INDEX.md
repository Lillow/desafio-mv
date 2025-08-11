# Celula Financeiro & Controladoria — Scripts e Documentação

Este diretório contém os **scripts SQL (Oracle / PL/SQL)** e o **README** principal do projeto.

## Estrutura
- `README.md` — guia de execução, arquitetura, padrões e boas práticas.
- `scripts/pkg_financeiro.sql` — package com **procedure** para registrar movimentação e **function** para cálculo de taxa por período.
- `scripts/trg_movimento.sql` — trigger para ajustar `DATA_HORA` na inserção em `MOVIMENTO` quando não informado.
- `scripts/teardown.sql` — limpeza segura dos objetos (DROP com tratamento).

> Observação: Os nomes de tabela/colunas consideram o padrão Oracle **UPPERCASE**. Se seu schema estiver com nomes diferentes, ajuste os scripts.