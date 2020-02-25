package server;

import java.net.Socket;

public class ClientInfo {

	private String name;
	private int id;
	private Socket socket;

	public ClientInfo(String name, int id, Socket socket) {
		super();
		this.name = name;
		this.id = id;
		this.socket = socket;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

}
