package br.com.desafio.celula_financeiro_controladoria.domain.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.desafio.celula_financeiro_controladoria.domain.dto.MovimentoDTO;
import br.com.desafio.celula_financeiro_controladoria.domain.service.MovimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimentos")
@RequiredArgsConstructor
public class MovimentoController {

    private final MovimentoService movService;

    @PostMapping
    public ResponseEntity<?> receberMovimento(@RequestBody @Valid MovimentoDTO dto) {
        var m = movService.registrarMovimento(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", m.getId()));
    }
}
