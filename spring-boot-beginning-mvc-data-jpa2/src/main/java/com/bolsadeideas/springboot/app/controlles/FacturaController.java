package com.bolsadeideas.springboot.app.controlles;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bolsadeideas.springboot.app.models.entity.Cliente;
import com.bolsadeideas.springboot.app.models.entity.Factura;
import com.bolsadeideas.springboot.app.models.entity.ItemFactura;
import com.bolsadeideas.springboot.app.models.entity.Producto;
import com.bolsadeideas.springboot.app.models.service.IClienteService;

@Controller
@SessionAttributes("factura") //mantenemos el objeto factura dentro de una session hasta que se procesa el formulario y se envia al metodo guardar. Metodo Guardar persite la factura en la BD
@RequestMapping("/factura")
public class FacturaController 
{
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private IClienteService clienteService;
	
	@GetMapping("/ver/{id}")
	public String ver(@PathVariable(value="id")Long id, Model model, RedirectAttributes flash) 
	{
		//Factura factura = clienteService.findFacturaById(id);
		Factura factura = clienteService.fecthByIdWithClienteWithItemFacturaWithProducto(id);
		if(factura == null)
		{
			flash.addFlashAttribute("error", "La factura no existe en la base de datos");
			return "redirect:/listar";
		}
		
		model.addAttribute("factura", factura);
		model.addAttribute("titulo", "Factura: ".concat(factura.getDescripcion()));
		return "factura/ver";
	}
	
	// Metodo que despliega el formulario Factura en la vista
	@GetMapping("/form/{clienteId}")
	public String crear(@PathVariable(value="clienteId") Long clienteId, Map<String, Object> model, RedirectAttributes flash)
	{
		Cliente cliente = clienteService.findOne(clienteId);
		if(cliente == null)
		{
			flash.addFlashAttribute("error", "El cliente no existen en la Base de Datos");
			return "redirect:/listar";
		}
		
		Factura factura = new Factura();
		factura.setCliente(cliente);
		
		//A traves del model pasamos el objeto factura a la vista  form.html
		model.put("factura", factura);
		model.put("titulo", "Crear factura");
		
		return "factura/form";
	}
	
	/**
	 * Metodo para la busqueda autocomplete de un producto
	 * url: cargar producto
	 * @PathVariable: se pasa el texto (request.term) que es el nombre del producto a buscar
	 * produces: Es la respuesta (salida) de tipo application json
	 */
	@GetMapping(value="/cargar-productos/{term}", produces= {"application/json"})
	public @ResponseBody List<Producto> cargarProductos(@PathVariable String term)
	{
		return clienteService.findByNombre(term);
	}
	
	/**
	 * Con @RequestParam se obtienen los id's y cantidad de productos en las lineas de factura
	 * @param factura : objeto que se inyecta automaticamente
	 * @param itemId :  id de los productos adicionamos al detalle de la factura 
	 * @param cantidad  cantidad del producto de cada linea de la factura 
	 * @param flash
	 * @Valid : Habilita la validacion en el objeto Factura de forma automatica
	 * @param result : Variable de tipo BindingResult que verifica que existieron errores en la validacion de la factura
	 * @param model : Variable de tipo Model que permite pasarle datos a la vista
	 * @return
	 */
	@PostMapping("/form")
	public String guardar(@Valid Factura factura, BindingResult result, Model model, @RequestParam(name="item_id[]", required=false) Long[] itemId,
			@RequestParam(name="cantidad[]", required = false) Integer[] cantidad, RedirectAttributes flash,
			SessionStatus status)
	{
		// validad: Preguntamos si tiene errores la factura
		if(result.hasErrors())
		{
			model.addAttribute("titulo", "Crear factura");
			return "factura/form";
		}
		
		// validamos que las lineas del detalle sean distintas a cero
		if(itemId == null || itemId.length == 0)
		{
			model.addAttribute("titulo", "Crear factura");
			model.addAttribute("error", "Error: La factura debe contener como minimo 1 linea");
			return "factura/form";
		}
		
		log.info(String.valueOf(itemId.length));
		
		//Loop For x los id de los productos
		for(int i = 0; i < itemId.length; i++)
		{
			// por cada linea del detalle obtenemos el producto
			Producto producto = clienteService.findProductoById(itemId[i]);
			//generamos lineas de facturas
			ItemFactura linea = new ItemFactura();
			linea.setCantidad(cantidad[i]);
			linea.setProducto(producto);
			//agregamos la linea del detalle a la factura
			factura.addItemFactura(linea);
			
			/*degub reason*/
			log.info("ID: " + itemId[i].toString() + ", cantidad: " + cantidad[i].toString());
		}
		// guardamos la factura con sus lineas y productos
		clienteService.saveFactura(factura);
		//finalizamos el SessionAtttributes y eliminamos la factura de la Session
		status.setComplete();
		flash.addFlashAttribute("success","Factura creada con exito");
		return "redirect:/ver/" + factura.getCliente().getId();
	}
	
	
	@GetMapping("/eliminar/{id}")
	public String eliminar(@PathVariable(value="id")Long id, RedirectAttributes flash)
	{
		Factura factura = clienteService.findFacturaById(id);
		if(factura != null)
		{
			clienteService.deleteFactura(id);
			flash.addFlashAttribute("success", "Factura eliminada con exito");
			return "redirect:/ver/" + factura.getCliente().getId();
		}
		flash.addFlashAttribute("error", "La factura no existe en la BD, no se pudo eliminar");
		return "redirect:/listar";
	}
}
