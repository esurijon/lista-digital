package com.surix.ld.model;


@SuppressWarnings("serial")
public class EntityNotFoundException extends EntityException {

	private String name;
	private Class<? extends FileEntity> type;

	public EntityNotFoundException(String name, Class<? extends FileEntity> type) {
		super("Cannot find entity " + name + " of type " + type.toString());
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<? extends FileEntity> getType() {
		return type;
	}
}
