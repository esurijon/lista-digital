package com.surix.ld.model;

import java.util.HashSet;
import java.util.Set;

public class Permissions {

	public enum Permission {
		READ_ENABLED,
		WRITE_ENABLED
	}
	
	Set<Permission> permissions = new HashSet<Permission>();

	public Permissions() {
	}

	public Permissions(Permissions permission) {
		this.permissions.addAll(permission.permissions);
	}

	public Permissions(Permission perm) {
		this.permissions.add(perm);
	}

	public boolean implies(Permissions permission) {
		return permissions.containsAll(permission.permissions);
	}

	public boolean implies(Permission permission) {
		return permissions.contains(permission);
	}

	public Permissions compose(Permissions permissions) {
		this.permissions.addAll(permissions.permissions);
		return this;
	}
	
	public Permissions compose(Permission permission) {
		this.permissions.add(permission);
		return this;
	}

	public Permissions mask(Permissions permissions) {
		this.permissions.retainAll(permissions.permissions);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Permission permission : permissions) {
			sb.append(permission.toString()).append('|');
		}
		return sb.toString();
	}
}
