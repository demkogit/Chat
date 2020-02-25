package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MultiServer {

	private static ArrayList<MessageServerThread> messageServers = new ArrayList<>();
	private static ArrayList<FileServerThread> fileServers = new ArrayList<>();

	private static ArrayList<ChatRoom> chatRooms = new ArrayList<>();
	
	private static int messagePort = 3000;
	private static int filePort = 3001;

	private static int ClientId = 0;
	private static int RoomId = 0;

	public static void main(String[] args) {

		new Thread(new Runnable() {
			ServerSocket serverSocket = null;

			@Override
			public void run() {
				try {
					serverSocket = new ServerSocket(messagePort);

					System.out.println("Server start on " + messagePort);
					chatRooms.add(new ChatRoom(RoomId++, "General"));
					while (true) {
						Socket socket = serverSocket.accept();
						System.out.println("Клиент подключился на " + messagePort);
						MessageServerThread thread = new MessageServerThread(socket, ClientId++, 0);
						chatRooms.get(0).getMessageServers().add(thread);
						messageServers.add(thread);				
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						serverSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();

		new Thread(new Runnable() {
			ServerSocket serverSocket = null;

			@Override
			public void run() {
				try {
					serverSocket = new ServerSocket(filePort);

					System.out.println("Server start on " + filePort);

					while (true) {
						Socket socket = serverSocket.accept();
						System.out.println("Клиент подключился на " + filePort);
						fileServers.add(new FileServerThread(socket));
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						serverSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}).start();

	}

	public static ArrayList<MessageServerThread> getMessageServers() {
		return messageServers;
	}

	public static ArrayList<FileServerThread> getFileServers() {
		return fileServers;
	}
	
	public static ArrayList<ChatRoom> getChatRooms(){
		return chatRooms;
	}

	public static ChatRoom createNewRoom(String name) {
		ChatRoom newRoom = new ChatRoom(RoomId++, name);
		chatRooms.add(newRoom);
		return newRoom;
	}
}
