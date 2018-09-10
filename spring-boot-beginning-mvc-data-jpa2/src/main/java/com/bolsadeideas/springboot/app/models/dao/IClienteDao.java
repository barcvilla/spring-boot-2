package com.bolsadeideas.springboot.app.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.bolsadeideas.springboot.app.models.entity.Cliente;

public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long> {
	
	/**
	 * Esta consulta generara un problema: Cuando consultamos un cliente que no tiene facturas aun, a pesar de que el
	 * cliente existe en la bd este query no traera ningun registro del cliente. El inner join exige que el cliente
	 * se encuentra tambien en la tabla facturas, si solo esta en la tabla cliente pero no en la de facturas entonces
	 * no trae nada, ni el cliente y obviamente ni facturas ya que no tiene  facturas.
	 * La solucion es utilizar el: left join fetch
	 * left join fetch es JPA en SQL equivale a left outer join
	 */
	@Query("select c from Cliente c left join fetch c.facturas f where c.id =?1")
	public Cliente fetchByIdWithFacturas(Long id);
}
