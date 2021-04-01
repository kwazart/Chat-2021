package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

public class ConsoleServer {
	private Vector<ClientHandler> users;

	public ConsoleServer() {
		users = new Vector<>();
		ServerSocket server = null; // наша сторона
		Socket socket = null; // удаленная (remote) сторона

		try {
			AuthService.connect();
			server = new ServerSocket(6001);
			System.out.println("Server started");

			while (true) {
				socket = server.accept();
				System.out.printf("Client [%s] connected\n", socket.getInetAddress());
				new ClientHandler(this, socket);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			AuthService.disconnect();
		}
	}

	public void subscribe(ClientHandler client) {
		users.add(client);
	}

	public void unsubscribe(ClientHandler client) {
		users.remove(client);
	}


	public void broadcastMessage(String str) {
		for (ClientHandler c : users) {
			c.sendMsg(str);
		}
	}
}
