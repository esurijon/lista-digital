package com.surix.ld.model;

public class User {

	private String id;
	private String email;
	private Role role;

	public User(String id, String email, Role role) {
		this.id = id;
		this.email = email;
		this.role = role;
	}

	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public Role getRole() {
		return role;
	}

	public String toXml() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><user><role type=\""+ role +"\">" + role.getLabel() + "</role><email>" + email + "</email></user>";
	}
}
