package com.bolsadeideas.springboot.app.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.bolsadeideas.springboot.app.models.entity.Producto;

public interface IProductoDao extends CrudRepository<Producto, Long> {
	//definimos un metodo que realiza una consulta en base al nombre del producto
	@Query("select p from Producto p where p.nombre like %?1%")
	public List<Producto> findByNombre(String term);
	
	/**
	 * Buscamos por el nombre del producto aplicando IgnoreCase. Automaticamente Spring Data realizara la consulta
	 * debido a que usamos el nombre de metodo findByNombreLikeIgnoreCase
	 * @param term
	 * @return
	 */
	public List<Producto> findByNombreLikeIgnoreCase(String term);
}
