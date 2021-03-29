package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
	private ConsoleServer server;
	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public ClientHandler(ConsoleServer server, Socket socket) {
		try {
			this.server = server;
			this.socket = socket;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());

			new Thread(() -> {
				try {
					while (true) {
						String str = in.readUTF();
						if ("/end".equals(str)) {
							out.writeUTF("/serverClosed");
							System.out.printf("Client [%s] disconnected\n", socket.getInetAddress());
							break;
						}
						System.out.printf("Client [%s] - %s\n", socket.getInetAddress(), str);
						server.broadcastMessage(str);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendMsg(String msg) {
		try {
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
