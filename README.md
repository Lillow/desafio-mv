# 📌 Celula Financeiro & Controladoria

Este projeto foi desenvolvido como parte de um **processo seletivo** para demonstrar conhecimentos técnicos em **Java, Spring Boot e boas práticas de desenvolvimento de software**.
A aplicação simula operações de controle financeiro e de contas de clientes, incluindo **movimentações, cálculos de taxas e integração com banco de dados Oracle via PL/SQL**.

## 📖 Contexto do Projeto
O sistema gerencia **clientes, contas e movimentações** e fornece relatórios de saldo e receita. Regras de precificação por janelas de 30 dias:
- **1..10** movimentações → **R$ 1,00** por movimentação
- **11..20** movimentações → **R$ 0,75** por movimentação
- **>20** movimentações → **R$ 0,50** por movimentação

## 🛠 Tecnologias
- Java 17, Spring Boot 3 (Web, Data JPA, JDBC)
- H2 (perfil de dev), Oracle (produção/integração)
- JUnit 5
- Maven, Lombok

## 🚀 Como Executar
### Ambiente H2 (dev)
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### Ambiente Oracle
1) Ajuste `application-oracle.yml` com URL/usuário/senha.
2) Execute os scripts em `scripts/` no seu schema Oracle:
   - `teardown.sql` (opcional)
   - `pkg_financeiro.sql`
   - `trg_movimento.sql`
3) Suba a aplicação:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=oracle
```

## 🔗 Integração Java ↔ PL/SQL (exemplo)
```java
var call = new SimpleJdbcCall(jdbcTemplate)
    .withCatalogName("PKG_FINANCEIRO")
    .withFunctionName("CALCULAR_TAXA_CLIENTE");

BigDecimal total = call.executeFunction(BigDecimal.class, Map.of(
    "P_CLIENTE_ID", clienteId,
    "P_INICIO", Date.valueOf(inicio),
    "P_FIM", Date.valueOf(fim)
));
```

## 📏 Boas Práticas
- Camadas: controller, service, repository, infra
- DTOs, validação, tratamento global de erros, soft delete
- Testes focados em regras de negócio e endpoints

## 🎯 Padrões
- DTO, Repository, Service Layer, DAO (PL/SQL), Soft Delete
- Preparado para Strategy no cálculo de taxas

## 👨‍💻 Autor
- [Danillo Silva](https://github.com/Lillow)
Projeto desenvolvido para desafio em processo seletivo na MV.
