package com.ufes.compiladores.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ufes.compiladores.dto.ResponseDTO;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
			new ResponseDTO("success", 1, 2),
			new ResponseDTO("success", 3, 4),
			new ResponseDTO("success", 5, 6)
		};
	}	
}