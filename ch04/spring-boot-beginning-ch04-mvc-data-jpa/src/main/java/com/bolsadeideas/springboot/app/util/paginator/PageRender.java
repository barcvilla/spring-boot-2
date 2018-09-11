package com.bolsadeideas.springboot.app.util.paginator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Clase generica que calcula la paginacion de registros que pueden ser, clientes, productos, facturas, etc
 * y muestra el Paginador
 * @author PC
 *
 */
public class PageRender<T> {
	
	private String url;
	private Page<T> page;
	private int totalPaginas;
	private int numElementosPorPagina;
	private int paginaActual;
	private List<PageItem> paginas;
	
	public PageRender(String url, Page<T> page) {
		this.url = url;
		this.page = page;
		numElementosPorPagina = page.getSize();
		totalPaginas = page.getTotalPages();
		paginaActual = page.getNumber() + 1; //la pagina parte desde cero, por lo que hay adicionar un numero
		this.paginas = new ArrayList<PageItem>();
		
		int desde, hasta;
		
		/**
		 * Calculos para el paginador
		 */
		if(totalPaginas <= numElementosPorPagina)
		{
			// Siempre que el totalPaginas sea menor o igual a numElementosPorPagina Mostramos el paginador completo
			desde = 1;
			hasta = totalPaginas;
		}
		else // Empezamos a navegar por rangos
		{
			/**
			 * 
			 * Alternativas: Rango mas para el lado del inicio / Rango mas para el lado del final
			 * case: totalPaginas = 25 y numElementosPorPagina = 10 y paginaActual = 1
			 */
			if(paginaActual <= numElementosPorPagina / 2)
			{
				desde = 1;
				hasta = numElementosPorPagina;
			}
			//totalPaginas=25 y numElementospPorPagina=10 / 2 = 5 (25-5)=20
			else if(paginaActual >= totalPaginas - numElementosPorPagina / 2)
			{
				desde = totalPaginas - numElementosPorPagina + 1;
				hasta = numElementosPorPagina;
			}
			else
			{
				desde = paginaActual - numElementosPorPagina / 2;
				hasta = numElementosPorPagina;
			}
		}
		
		for(int i = 0; i < hasta; i++)
		{
			paginas.add(new PageItem(desde + i, paginaActual == desde + i));
		}
	}

	public String getUrl() {
		return url;
	}

	public int getTotalPaginas() {
		return totalPaginas;
	}

	public int getPaginaActual() {
		return paginaActual;
	}

	public List<PageItem> getPaginas() {
		return paginas;
	}
	
	
	public boolean isFirst()
	{
		return page.isFirst();
	}
	
	public boolean isLast()
	{
		return page.isLast();
	}
	
	public boolean isHasNext()
	{
		return page.hasNext();
	}
	
	public boolean isHasPrevious()
	{
		return page.hasPrevious();
	}
}
