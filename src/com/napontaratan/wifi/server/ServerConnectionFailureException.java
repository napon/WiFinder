package com.napontaratan.wifi.server;

public class ServerConnectionFailureException extends Exception {
	public ServerConnectionFailureException() {
		super();
	}
	
	public ServerConnectionFailureException(String message) {
		super(message);
	}
}
