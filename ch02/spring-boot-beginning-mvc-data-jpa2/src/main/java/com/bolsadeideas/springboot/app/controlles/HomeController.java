package com.bolsadeideas.springboot.app.controlles;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * SpringMVC controller con un metodo handler para la URL /, el cual retorna la vista llamada index.html
 * 
 */

@Controller
public class HomeController {
	
	@RequestMapping("/")
	public String home(Model model)
	{
		return "index.html";
	}
}
