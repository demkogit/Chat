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

public class FileServerThread extends Thread {
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private int clientId;
	private int roomId;
	private String name;
	// еще должен быть массив файлов
	

	public FileServerThread(Socket socket, int clientId, int roomId) {
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

				if (msg.equals("stop")) {
					this.disconnect();
					break;
				}
				System.out.println("Echoing: " + msg);

				JSONParser parser = new JSONParser();
				JSONObject json = new JSONObject();
				int type = -1;

				try {
					json = (JSONObject) parser.parse(msg);

					type = ((Long) json.get("type")).intValue();

					switch (type) {
					case 0:

						break;
					case 1:

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

	private void disconnect() {
		try {
			if (!socket.isClosed()) {
				socket.close();
				in.close();
				out.close();

				this.interrupt();
				MultiServer.getFileServers().remove(this);
				ChatRoom room = MultiServer.getChatRooms().stream().filter(e -> e.getId() == roomId).findFirst()
						.orElse(null);
				room.getFileServers().remove(this);
			}
		} catch (IOException ignored) {
		}
	}
}
