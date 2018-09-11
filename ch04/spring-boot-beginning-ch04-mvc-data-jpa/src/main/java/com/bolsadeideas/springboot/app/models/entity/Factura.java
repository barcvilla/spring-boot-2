package com.bolsadeideas.springboot.app.models.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import javax.validation.constraints.NotEmpty;

@Entity
@Table(name="facturas")
public class Factura implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotEmpty
	private String descripcion;
	private String observacion;
	
	@Temporal(TemporalType.DATE)
	@Column(name="create_at")
	private Date createAt;
	
	/**
	 * El Atributo cliente importante para la relacion entre Factura y Cliente
	 * @ManyToOne : Muchas Facturas tiene un cliente
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	private Cliente cliente;
	
	/**
	 * Atributo items que permite la relacion con la entidad ItemFactura
	 * @OneToMany : Una factura contiene muchas lineas de ItemFactura
	 * @JoinColumn(name="factura_id") Llave foranea (factura_id) que esta presente en la tabla facturas_items para relacionar Factura - ItemFactura
	 */
	@OneToMany(fetch=FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name="factura_id")
	private List<ItemFactura> items; 
	
	public Factura()
	{
		this.items = new ArrayList<ItemFactura>();
	}
	
	// metodo que genera un nueva fecha y se ejecuta antes de Persistir la factura/
	@PrePersist
	public void prePersist()
	{
		createAt = new Date();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescripcion() {
		return descripcion;
	}
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	public String getObservacion() {
		return observacion;
	}
	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	public Date getCreateAt() {
		return createAt;
	}
	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	public void setItems(List<ItemFactura> items)
	{
		this.items = items;
	}
	
	public List<ItemFactura> getItems()
	{
		return items;
	}
	
	public void addItemFactura(ItemFactura item)
	{
		this.items.add(item);
	}
	
	public Double getTotal()
	{
		Double total = 0.0;
		int size = items.size();
		
		for(int i = 0; i < size; i++)
		{
			total += items.get(i).calcularImporte();
		}
		
		return total;
	}
}
