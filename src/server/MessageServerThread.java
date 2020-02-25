package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Base64;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MessageServerThread extends Thread {
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private int clientId;
	private int roomId;
	private String name;

	public MessageServerThread(Socket socket, int clientId, int roomId) {
		this.socket = socket;
		this.clientId = clientId;
		this.roomId = roomId;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		String msg;

		while (!socket.isClosed()) {
			try {
				msg = in.readLine();
				System.out.println("Echoing from " + name + " : " + msg);

				JSONParser parser = new JSONParser();
				JSONObject json = new JSONObject();

				try {
					json = (JSONObject) parser.parse(msg);
					int type = ((Long) json.get("type")).intValue();
					switch (type) {
					// Подключение
					case 0:
						// Получить {type: 0, clientName:"name"}
						// Отослать {type: 4, clientId: this.clientId, msg: "Hello, name!"}
						name = (String) json.get("clientName");
						broadcast(name + " connected to chat!");
						send("{\"type\":4, \"clientId\":" + clientId + ", \"msg\":\"Hello, " + name + "!\"}");
						break;
					// Отключение
					case 1:
						// Получить {type: 1}
						this.disconnect();
						break;
					// Создание комнаты
					case 2:
						// Получить {type: 2, clientId: this.clientId, roomName: "roomName"}
						// Отослать {type: 4, clientId:this.clientId, msg: "Room was created name!"}
						String roomName = (String) json.get("roomName");
						createRoom(roomName);
						break;
					// Подключение к другой комнате
					case 3:
						// Получить {type: 3, clientId: this.clientId, newRoomId: newRoomId}
						// Отослать {type: 3, newRoomId: newRoomId, msg: "Connected to room " +
						// roomName}
						int newRoomId = ((Long) json.get("newRoomId")).intValue();
						changeRoom(newRoomId);
						break;
					// Отправка сообщений в комнату
					case 4:
						// Получить {type: 4, msg: "msg"}
						// Отослать ВСЕМ {type: 4, msg: "msg"}
						System.out.println("начало парсинга сообщения");
						String message = (String) json.get("msg");
						System.out.println("конец парсинга сообщения " + message);
						broadcast(message);
						break;
					default:
						throw new IllegalArgumentException("Unexpected value: " + type);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private void send(String msg) {
		try {
			out.write(msg + "\n");
			out.flush();
		} catch (IOException ignored) {
		}

	}

	private void broadcast(String msg) {

		ChatRoom currentRoom = MultiServer.getChatRooms().stream().filter(e -> e.getId() == roomId).findFirst()
				.orElse(null);
		System.out.println("Текущая комната " + currentRoom.getId());
		for (MessageServerThread server : currentRoom.getMessageServers()) {
			server.send("{\"type\":4, \"msg\":\"" + msg + "\"}");
		}
	}

	private void createRoom(String roomName) {
		ChatRoom newRoom = MultiServer.createNewRoom(roomName);
		changeRoom(newRoom.getId());
		// broadcast("Chat room was created!");
	}

	private void changeRoom(int newRoomId) {

		ChatRoom newRoom = MultiServer.getChatRooms().stream().filter(e -> e.getId() == newRoomId).findFirst()
				.orElse(null);

		if (newRoom != null) {
			ChatRoom oldRoom = MultiServer.getChatRooms().stream().filter(e -> e.getId() == roomId).findFirst()
					.orElse(null);
			newRoom.getMessageServers().add(this);
			oldRoom.getMessageServers().remove(this);
			broadcast("User " + name + " leave this room");
			roomId = newRoomId;
			broadcast("User " + name + " connected to " + newRoom.getName());
			send("{\"type\":3, \"roomId\":" + newRoomId + ", \"roomName\":\"" + newRoom.getName() + "\"}");
		} else {
			send("{\"type\":4, \"msg\":\"Ошибка: комната не найдена\"}");
		}
	}

	private void disconnect() {
		try {
			if (!socket.isClosed()) {
				System.out.println("server closed");
				socket.close();
				in.close();
				out.close();

				
				/*
				 * for (MessageServerThread vr : MultiServer.getMessageServers()) { if
				 * (vr.equals(this)) vr.interrupt();
				 * MultiServer.getMessageServers().remove(this); }
				 */
				
				this.interrupt();
				MultiServer.getMessageServers().remove(this);
				ChatRoom room = MultiServer.getChatRooms().stream().filter(e -> e.getId() == roomId).findFirst()
						.orElse(null);
				room.getMessageServers().remove(this);


			}
		} catch (IOException ignored) {
		}
	}
}
