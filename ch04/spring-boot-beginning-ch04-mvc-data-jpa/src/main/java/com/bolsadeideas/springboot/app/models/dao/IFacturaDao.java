package com.bolsadeideas.springboot.app.models.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.bolsadeideas.springboot.app.models.entity.Factura;

public interface IFacturaDao extends CrudRepository<Factura, Long> {
	
	//Utilizamos JOIN FETCH para en una sola consulta recuperar la factura, cliente, lineas facturas, productos
	//anotacion que permite personalziar una consulta
	/**
	 * 1. Seleccionar los registros de la factura (select f from factura f)
	 * 2. Seleccionar el cliente de factura (join fetch f.ciente c )
	 * 3. Seleccionar las lineas de factura (join fetch f.items l)
	 * 4. Seleccionar el producto de cada linea de factura (join fecth l.producto)
	 * 5. Donde la factura sea igual al id de factura (f.id = ?1)
	 */
	@Query("select f from Factura f join fetch f.cliente c join fetch f.items l join fetch l.producto where f.id =?1")
	public Factura fecthByIdWithClienteWithItemFacturaWithProducto(Long id);
}
