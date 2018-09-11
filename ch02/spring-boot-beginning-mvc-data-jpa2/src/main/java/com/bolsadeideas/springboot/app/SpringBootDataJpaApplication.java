package com.bolsadeideas.springboot.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bolsadeideas.springboot.app.models.service.IUploadService;

/**
 * Application Entry Point Class
 */
@SpringBootApplication
public class SpringBootDataJpaApplication implements CommandLineRunner {

	@Autowired
	IUploadService uploadFileService;
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBootDataJpaApplication.class, args);
	}
	
	/**
	 * Indicamos a la aplicacion que cree el directorio uploads cuando es iniciada.
	 */
	@Override
	public void run(String... arg0) throws Exception {
		
		uploadFileService.deleteAll();
		uploadFileService.init();
	}
}
