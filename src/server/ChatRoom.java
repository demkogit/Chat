package server;

import java.util.ArrayList;

public class ChatRoom {

	private ArrayList<MessageServerThread> messageServers;
	private ArrayList<FileServerThread> fileServers;
	private int id = 0;
	private String name;

	public ChatRoom(int id, String name) {
		this.id = id;
		this.name = name;
		messageServers = new ArrayList<MessageServerThread>();
		fileServers = new ArrayList<FileServerThread>();
	}

	public ArrayList<MessageServerThread> getMessageServers() {
		return messageServers;
	}
	
	public ArrayList<FileServerThread> getFileServers(){
		return fileServers;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
