package com.ufes.compiladores.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufes.compiladores.dto.CodeDTO;
import com.ufes.compiladores.models.Token;
import com.ufes.compiladores.service.AnalizadorLexicoService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class CompiladorController {

	@Autowired
	private AnalizadorLexicoService lexicalAnalyzerService;

	@PostMapping("/analyze")
	public ResponseEntity<List<Token>> analyzeCode(@RequestBody CodeDTO codeDTO) {
		List<Token> tokens = lexicalAnalyzerService.analisar(codeDTO.getCode());
		return ResponseEntity.ok(tokens);
	}
}