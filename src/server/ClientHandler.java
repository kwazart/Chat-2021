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
	private String nickname;

	public ClientHandler(ConsoleServer server, Socket socket) {
		try {
			this.server = server;
			this.socket = socket;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());

			new Thread(() -> {
				try {
					// auth - /auth login pass
					while (true) {
						String str = in.readUTF();
						if (str.startsWith("/auth ")) {
							String[] tokens = str.split(" ");
							String nick = AuthService.getNicknameByLoginAndPassword(tokens[1], tokens[2]);

							// запрет аутентификации с одной учётной записи
							if (server.isNickUsed(nick)) {
								sendMsg(String.format("User %s already authorized", nick));
								sendMsg("/serverClosed");
								continue;
							}
							if (nick != null) {
								sendMsg("/auth-OK");
								setNickname(nick);
								server.subscribe(ClientHandler.this);
								break;
							} else {
								sendMsg("Wrong login/password");
							}
						}
					}

					while (true) {
						String str = in.readUTF();
						if ("/end".equals(str)) {
							out.writeUTF("/serverClosed");
							System.out.printf("Client [%s] disconnected\n", socket.getInetAddress());
							break;
						}
						if (str.startsWith("@")) {
							String[] splitMsg = str.split(" ", 2);
							if (splitMsg.length == 2) {
								server.sendPrivateMessage(nickname, splitMsg[0].replaceFirst("@", ""), splitMsg[1]);
							} else {
								System.out.printf("Client [%s]: %s\n", socket.getInetAddress(), str);
								server.broadcastMessage(nickname + ": " + str);
							}
						} else {
							System.out.printf("Client [%s]: %s\n", socket.getInetAddress(), str);
							server.broadcastMessage(nickname + ": " + str);
						}
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
					server.unsubscribe(this);
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setNickname(String nick) {
		this.nickname = nick;
	}

	public String getNickname() {
		return nickname;
	}

	public void sendMsg(String msg) {
		try {
			out.writeUTF(msg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
