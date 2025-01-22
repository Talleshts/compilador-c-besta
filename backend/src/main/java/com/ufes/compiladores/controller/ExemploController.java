package com.ufes.compiladores.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufes.compiladores.dto.ResponseDTO;


@RestController
@RequestMapping("/api")
public class ExemploController {
	@GetMapping("/hello")
	public String hello() {
		return "Hello from backend!";
	}

	@PostMapping("/post")
	public ResponseDTO[] postMethodName(@RequestBody String entity) {
		return new ResponseDTO[] {
			new ResponseDTO("Erro Léxico 1;", 1, 2),
			new ResponseDTO("Erro Léxico 2;", 3, 4),
			new ResponseDTO("Erro Léxico 3;", 5, 6)
		};
	}	
}