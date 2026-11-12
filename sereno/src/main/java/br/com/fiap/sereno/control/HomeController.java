package br.com.fiap.sereno.control;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@Controller
public class HomeController {

	@GetMapping({ "/", "/login" })
	public String inicio() {
		return "redirect:/swagger-ui.html";
	}

}
