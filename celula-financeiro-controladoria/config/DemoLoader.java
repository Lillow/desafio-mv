package br.com.desafio.celula_financeiro_controladoria.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Profile("h2")
public class DemoLoader implements CommandLineRunner {
    @Override
    public void run(String... args) {
        // TODO: inserir clientes, contas e movimentações de exemplo
        // Ex: repositories.save(...); etc.
    }
}
