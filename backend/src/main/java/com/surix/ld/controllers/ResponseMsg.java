package com.surix.ld.controllers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name="response")
public class ResponseMsg {

	private String status;
	private String message;
	private List list;

	public ResponseMsg() {
	}

	public ResponseMsg(List list) {
		this.list = list;
	}

	public ResponseMsg(String status, String message) {
		this.status = status;
		this.message = message;
	}

	@XmlElement(name = "status")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@XmlElement(name = "message")
	public String getMessage() {
		return message;
	}

	@XmlElement(name = "item")
	public List getList() {
		return list;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
