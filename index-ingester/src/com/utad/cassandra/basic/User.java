package com.utad.cassandra.basic;

public class User {

	public String id;
	public String email;
	public String nombre;
	public String cp;
	public String calle;

	public User(String id, String email, String nombre, String cp,
			String calle) {
		this.id = id;
		this.email = email;
		this.nombre = nombre;
		this.cp = cp;
		this.calle = calle;
	}
}