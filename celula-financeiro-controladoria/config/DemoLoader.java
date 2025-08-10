package br.com.desafio.celula_financeiro_controladoria.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Profile("h2")
@RequiredArgsConstructor
public class DemoLoader implements CommandLineRunner {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private MovimentoService movimentoService;

    @Override
    public void run(String... args) {
        // cria cliente, conta, e registra uma sequência de créditos/débitos
    }
}