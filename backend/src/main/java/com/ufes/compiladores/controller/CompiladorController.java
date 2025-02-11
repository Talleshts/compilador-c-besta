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
import com.ufes.compiladores.dto.ResponseDTO;
import com.ufes.compiladores.models.ErroSintatico;
import com.ufes.compiladores.models.Token;
import com.ufes.compiladores.service.AnalisadorSintaticoService;
import com.ufes.compiladores.service.AnalizadorLexicoService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class CompiladorController {

	@Autowired
	private AnalizadorLexicoService lexicalAnalyzerService;

	@Autowired
	private AnalisadorSintaticoService sintaticoAnalyzerService;

	@PostMapping("/analyze-lexica")
	public ResponseEntity<ResponseDTO> analyzeLexicaCode(@RequestBody CodeDTO codeDTO) {
		List<Token> tokens = lexicalAnalyzerService.analisar(codeDTO.getCode());
		ResponseDTO response = new ResponseDTO(tokens, null, null, null);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/analyze-sintatica")
	public ResponseEntity<ResponseDTO> analyzeSintaticaCode(@RequestBody CodeDTO codeDTO) {
		List<ErroSintatico> errors = sintaticoAnalyzerService.analisar(codeDTO.getCode());
		ResponseDTO response = new ResponseDTO(null, errors, null, null);
		return ResponseEntity.ok(response);
	}
}