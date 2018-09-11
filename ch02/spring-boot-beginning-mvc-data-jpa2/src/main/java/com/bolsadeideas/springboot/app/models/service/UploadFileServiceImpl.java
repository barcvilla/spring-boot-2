package com.bolsadeideas.springboot.app.models.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileServiceImpl implements IUploadService {

	private static final String UPLOADS_FOLDER = "uploads";

	@Override
	public Resource load(String filename) throws MalformedURLException {

		Path pathFoto = getPath(filename);
		Resource recurso = new UrlResource(pathFoto.toUri());

		if (!recurso.exists() && !recurso.isReadable()) {
			throw new RuntimeException("Error: No se puede cargar la imagen: " + pathFoto.toString());
		}
		return recurso;
	}

	@Override
	public String copy(MultipartFile file) throws IOException {
		// generamos un nombre unico para la foto subida al servidor
		String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
		// 2. Objeto Path donde se almacenaran las fotos
		// ruta en la raiz del proyecto: (uploads/"file_name")
		Path rootPath = getPath(uniqueFileName);

		// Path directorioResources =
		// Paths.get("src//main//resources//static//uploads");
		// String rootPath = directorioResources.toFile().getAbsolutePath();
		// 3. procesamos el contendio

		Files.copy(file.getInputStream(), rootPath);
		/**
		 * byte[] bytes = foto.getBytes(); Path rutaCompleta = Paths.get(rootPath + "//"
		 * + foto.getOriginalFilename()); //4. escribimos los bytes de la foto hacia el
		 * directorio en el server Files.write(rutaCompleta, bytes);
		 */

		return uniqueFileName;
	}

	@Override
	public boolean delete(String filename) {
		//obtenemos la ruta absoluta de la foto del cliente
		Path rootPath = getPath(filename);
		//obtenemos la foto guardada en el servidor
		File archivo = rootPath.toFile();
		//validamos
		if(archivo.exists() && archivo.canRead())
		{
			//metodo delete de la clase File retorna un booleano
			if(archivo.delete())
			{
				return true;
			}
		}
		return false;
	}

	public Path getPath(String filename) {
		return Paths.get(UPLOADS_FOLDER).resolve(filename).toAbsolutePath();
	}

	@Override
	public void deleteAll() {
		
		FileSystemUtils.deleteRecursively(Paths.get(UPLOADS_FOLDER).toFile());
	}

	@Override
	public void init() throws IOException {
		
		Files.createDirectory(Paths.get(UPLOADS_FOLDER));
	}

}
