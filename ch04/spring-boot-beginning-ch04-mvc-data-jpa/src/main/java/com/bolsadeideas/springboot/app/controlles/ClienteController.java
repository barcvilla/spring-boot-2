package com.bolsadeideas.springboot.app.controlles;


import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.models.service.IClienteService;
import com.bolsadeideas.springboot.app.models.service.IUploadService;
import com.bolsadeideas.springboot.app.util.paginator.PageRender;

//Configuramos la clase como un controlador
@Controller
@SessionAttributes("cliente") // Cada vez que llamamos al metodo guardar o crear. En los atributos de la
								// session se almacenara el objeto cliente
public class ClienteController {

	@Autowired // buscamos un componente registrado en el contenedor que implemente la
				// interface IClienteDao
	// @Qualifier("clienteDaoJPA") //nos aseguramos de injectar la clase correcta
	// que implemente la interface
	private IClienteService clienteService;

	@Autowired
	IUploadService uploadFileService;

	/**
	 * Metodo para pasar la foto como recurso de manera programatica
	 */
	@GetMapping(value = "/uploads/{filename:.+}")
	public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
		Resource recurso = null;
		try {
			recurso = uploadFileService.load(filename);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"")
				.body(recurso);
	}

	/**
	 * Metodo para ver el detalle del cliente + foto
	 * 
	 * @PathVariable(value="id") Long id le pasamos como argumento el id del cliente
	 */
	@RequestMapping(value = "/ver/{id}", method = RequestMethod.GET)
	public String ver(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		//Cliente cliente = clienteService.findOne(id);
		//consulta obtimizada
		Cliente cliente = clienteService.fetchByIdWithFacturas(id);
		if (cliente == null) {
			flash.addFlashAttribute("error", "El cliente no existe en la Base de Datos");
			return "redirect:/listar";
		}

		// Pasamos el objeto Cliente a la vista
		model.put("cliente", cliente);
		model.put("titulo", "Detalle cliente: " + cliente.getNombre());

		return "ver";
	}

	@RequestMapping(value = "/listar", method = RequestMethod.GET)
	public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model) {
		/**
		 * page : numero de paginas actual. Esta variable se pasa por la URL
		 * (@RequestParam) es una param GET size : cantidad de elementos por pagina a
		 * mostrar
		 */
		Pageable pageRequest = PageRequest.of(page, 6);

		Page<Cliente> clientes = clienteService.findAll(pageRequest);

		PageRender<Cliente> pageRender = new PageRender<Cliente>("/listar", clientes);
		model.addAttribute("page", pageRender);
		model.addAttribute("titulo", "Listado de Cliente");
		// retornamos la lista de clientes con paginacion
		model.addAttribute("clientes", clientes);
		return "listar";
	}

	/**
	 * Metodo request para controlar el ingreso de un nuevo cliente. Mostramos un
	 * formulario al usuario. En la firma de nuestro metodo crear() podemos usar
	 * como parametro el objeto Model, pero tambien podemos usar un objeto de tipo
	 * Map<> como se muestra. La primera face consiste en mostrar el formulario al
	 * Usuario. La segunda fase consiste en el envio via submit los datos del
	 * formulario
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public String crear(Map<String, Object> model) {
		Cliente cliente = new Cliente();
		model.put("cliente", cliente);
		model.put("titulo", "Formulario de Clientes");
		return "form";
	}

	/**
	 * Metodo que recibe los datos desde el formulario cliente enviado por el
	 * usuario
	 * 
	 * @Valid activa la validacion al objeto mapeado en el form que es el Cliente
	 *        BindingResult valida el resultado @RequestParam("file") inyectamos un
	 *        archivo al servidor
	 */
	@RequestMapping(value = "/form", method = RequestMethod.POST)
	public String guardar(@Valid Cliente cliente, BindingResult result, Model model,
			@RequestParam("file") MultipartFile foto, RedirectAttributes flash, SessionStatus status) {
		if (result.hasErrors()) {
			model.addAttribute("titulo", "Formulario de Cliente");
			// si contiene errores retornamos al formulario form
			return "form";
		}

		/**
		 * Procedimiento para recibir el archivo (foto) 1. Adicionamos una carpeta
		 * externa es decir, en la raiz del proyecto
		 */
		if (!foto.isEmpty()) {
			/**
			 * Si se edita la foto del cliente por una actual, se debe registrar la nueva
			 * foto y a su vez eliminar la foto antigua en el servidor
			 */
			if (cliente.getId() != null && cliente.getId() > 0 && cliente.getFoto() != null
					&& cliente.getFoto().length() > 0) {
				uploadFileService.delete(cliente.getFoto());
			}

			String uniqueFilename = null;
			
			try 
			{
				uniqueFilename = uploadFileService.copy(foto);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}

			flash.addFlashAttribute("info", "Carga de la foto exitosamente " + uniqueFilename);
			// 5. Pasamos el nomnbre de la foto al objeto cliente para su almacenamiento en
			// la BD
			cliente.setFoto(uniqueFilename);
		}

		String mensajeFlash = (cliente.getId() != null ? "Cliente editado con exito" : "Cliente creado con exito");

		// llamamos a nuestros clase dao
		clienteService.save(cliente);
		// Cuando se terminar de realizar la operacion insertar o editar, eliminamos el
		// objeto cliente de la session
		status.setComplete();
		flash.addFlashAttribute("success", mensajeFlash);
		return "redirect:/listar";
	}

	/**
	 * @PathVariable nos inyecta el id del cliente
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/form/{id}", method = RequestMethod.GET)
	public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Cliente cliente = null;
		if (id > 0) {
			cliente = clienteService.findOne(id);
			if (cliente == null) {
				flash.addFlashAttribute("error", "No se pudo recuperar el cliente para la edicion");
				return "redirect:/listar";
			}
		} else {
			flash.addFlashAttribute("error", "El id del cliente no puede ser 0");
			return "redirect:/listar";
		}

		// Pasamos los datos a la vista
		model.put("cliente", cliente);
		model.put("titulo", "Editar Clientes");
		return "form";
	}

	@RequestMapping(value = "/eliminar/{id}", method = RequestMethod.GET)
	public String eliminar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes flash) {
		Cliente cliente = null;
		if (id > 0) {
			// Cuando se elimina el cliente de la BD tambien se debe eliminar su foto en el
			// servidor
			cliente = clienteService.findOne(id);

			clienteService.delete(id);
			flash.addFlashAttribute("success", "Cliente eliminado con exito");

			// metodo delete de la clase File retorna un booleano
			if (uploadFileService.delete(cliente.getFoto())) {
				flash.addFlashAttribute("info", "Foto " + cliente.getFoto() + " eliminada");
			}

		}
		return "redirect:/listar";
	}
}
