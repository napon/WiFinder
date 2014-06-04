package com.napontaratan.wifi.controller;

public class ServerConnectionFailureException extends Exception {
	public ServerConnectionFailureException() {
		super();
	}
	
	public ServerConnectionFailureException(String message) {
		super(message);
	}
}
