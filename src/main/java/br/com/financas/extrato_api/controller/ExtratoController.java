package br.com.financas.extrato_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ExtratoController {
    ResponseEntity<?> carregarExtrato(
            @PathVariable String banco,
            @RequestParam("file") MultipartFile file) throws IOException;
}
