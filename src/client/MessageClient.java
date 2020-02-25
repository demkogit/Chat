package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import server.FileServerThread;

public class MessageClient {
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private String address;
	private int port;
	private String clientName;
	private int clientId;
	private int roomId;
	private String roomName = "General";

	private ReadMsg ReadMsgThread;

	public MessageClient(String addr, int port, String name) {
		this.address = addr;
		this.port = port;
		this.clientName = name;
		try {
			this.socket = new Socket(addr, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			send("{\"type\":0, \"clientName\":\"" + name + "\"}");
			ReadMsgThread = new ReadMsg();
		} catch (IOException e) {
			// Сокет должен быть закрыт при любой
			// ошибке, кроме ошибки конструктора сокета:
			MessageClient.this.disconnect();
		}
		// В противном случае сокет будет закрыт
		// в методе run() нити.
	}

	public void disconnect() {
		try {
			if (!socket.isClosed()) {
				send("{\"type\":1}");
				socket.close();
				in.close();
				out.close();
				System.out.println("close");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onClick(String message) {

		String[] msgSplit = message.split(" ");

		if (message.startsWith("\\createRoom")) {
			if (msgSplit.length >= 2) {
				createChatRoom(msgSplit[1]);
			} else {
				ClientWindow.print("***Необходимо ввести имя комнаты***");
			}
		} else if (message.startsWith("\\changeRoom")) {
			if (msgSplit.length >= 2) {
				try {
					changeRoom(Integer.parseInt(msgSplit[1]));
				} catch (NumberFormatException e) {
					ClientWindow.print("***Идентификатор должен быть целым числом***");
				}
			} else {
				ClientWindow.print("***Необходимо ввести идентификатор комнаты***");
			}
		} else if (message.startsWith("\\peopleRoom")) {
			// getPeopleThisRoom();
		} else if (message.startsWith("\\people")) {
			// getPeople();
		} else if (message.startsWith("\\disconnect")) {
			disconnect();
		} else {
			sendMessage(message);
		}
	}
	
	public void send(String message) {
		try {
			System.out.println("send: " + message);
			out.write(message + "\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			MessageClient.this.disconnect();
		}
	}

	private void createChatRoom(String roomName) {
		send("{\"type\":2, \"roomName\":\"" + roomName + "\"}");
	}

	private void changeRoom(int newRoomId) {
		send("{\"type\":3, \"newRoomId\":" + newRoomId + "}");
	}

	private void sendMessage(String message) {
		send("{\"type\":4, " + "\"msg\":\"" + clientName + "(" + clientId + "): " + message + "\"}");
	}

	private class ReadMsg extends Thread {

		public ReadMsg() {
			start();
		}

		@Override
		public void run() {

			String message = "";
			try {
				while (!socket.isClosed()) {
					message = in.readLine();
					System.out.println("receive: " + message);
					JSONParser parser = new JSONParser();
					JSONObject json = new JSONObject();

					json = (JSONObject) parser.parse(message);

					int type = ((Long) json.get("type")).intValue();
					String msg = "";
					switch (type) {
					case 0:
						clientId = ((Long) json.get("clientId")).intValue();
						msg = (String) json.get("msg");
						break;
					case 3:
						roomId = ((Long) json.get("roomId")).intValue();
						roomName = (String) json.get("roomName");
						System.out.println("connected to " + roomName);
						break;
					case 4:
						msg = (String) json.get("msg");
						ClientWindow.print(msg);
						break;
					default:
						throw new IllegalArgumentException("Unexpected value: " + type);
					}
				}
				this.interrupt();

			} catch (IOException | ParseException e) {
				MessageClient.this.disconnect();
			}
		}
	}
}