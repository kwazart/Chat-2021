package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller {
	@FXML
	TextArea textArea;
	@FXML
	TextField textField;
	@FXML
	HBox bottomPanel;
	@FXML
	HBox upperPanel;
	@FXML
	TextField loginField;
	@FXML
	PasswordField passwordField;

	Socket socket;
	DataInputStream in;
	DataOutputStream out;

	public static final String ADDRESS = "localhost";
	public static final int PORT = 6001;

	private boolean isAuthorized;

	public void setAuthorized(boolean authorized) {
		this.isAuthorized = authorized;

		if (!isAuthorized) {
			upperPanel.setVisible(true);
			upperPanel.setManaged(true);

			bottomPanel.setVisible(false);
			bottomPanel.setManaged(false);
		} else {
			upperPanel.setVisible(false);
			upperPanel.setManaged(false);

			bottomPanel.setVisible(true);
			bottomPanel.setManaged(true);
		}
	}

	@FXML
	void sendMsg() {
		try {
			out.writeUTF(textField.getText());
			textField.clear();
			textField.requestFocus();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect() {
		try {
			socket = new Socket(ADDRESS, PORT);

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			new Thread(() -> {
				try {
					while (true) {
						String str  = in.readUTF();
						if ("/auth-OK".equals(str)) {
							setAuthorized(true);
							textArea.clear();
							break;
						} else {
							textArea.appendText(str + "\n");
						}
					}

					while (true) {
						String str  = in.readUTF();
						if ("/serverClosed".equals(str)) {
							disconnect();
						}
						textArea.appendText(str + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					setAuthorized(false);
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
			textArea.appendText("Connection refused\n");
		}
	}


	public void tryToAuth(ActionEvent actionEvent) {
		if (socket == null || socket.isClosed()) {
			connect();
		}
		try {
			out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
			loginField.clear();
			passwordField.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {

		try {
			if (socket != null || !socket.isClosed()) {
				out.writeUTF("/end");
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
