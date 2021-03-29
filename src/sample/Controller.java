package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
	@FXML
	TextArea textArea;

	@FXML
	TextField textField;

	Socket socket;
	DataInputStream in;
	DataOutputStream out;

	public static final String ADDRESS = "localhost";
	public static final int PORT = 6001;


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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			socket = new Socket(ADDRESS, PORT);

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());

			new Thread(() -> {
				try {
					while (true) {
						String str  = in.readUTF();
						if ("/serverClosed".equals(str)) {
							break;
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
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
