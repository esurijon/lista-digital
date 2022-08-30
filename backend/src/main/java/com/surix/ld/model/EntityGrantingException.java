package com.surix.ld.model;


@SuppressWarnings("serial")
public class EntityGrantingException extends EntityException {

	private String user;
	private Class<? extends FileEntity> entity;

	public EntityGrantingException(String user, Class<? extends FileEntity> entity) {
		super("Cannot grant access to user " + user + " to entity of type " + entity.toString());
		this.user = user;
		this.entity = entity;
	}

	public String getUser() {
		return user;
	}

	public Class<? extends FileEntity> getType() {
		return entity;
	}
}
