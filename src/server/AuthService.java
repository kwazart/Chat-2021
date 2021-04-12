package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AuthService {
	private static Connection connection;
	private static Statement statement;

	public static void connect() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:main.db");
			statement = connection.createStatement();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
	}

	public static int addUser(String login, String pass, String nickname) {
		try {
			String query = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
			PreparedStatement ps = connection.prepareStatement(query);
			ps.setString(1, login);
			ps.setInt(2, pass.hashCode());
			ps.setString(3, nickname);
			return ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String getNicknameByLoginAndPass(String login, String pass) {
		String query = String.format("select nickname, password from users where login='%s'", login);
		try {
			ResultSet rs = statement.executeQuery(query); // возвращает выборку через select
			int myHash = pass.hashCode();
			// кеш числа 12345
			// изменим пароли в ДБ на хеш от строки pass1

			if (rs.next()) {
				String nick = rs.getString(1);
				int dbHash = rs.getInt(2);
				if (myHash == dbHash) {
					return nick;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void disconnect() {
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	* Сохраняет чёрный список в БД
	*/
	public static void setBlacklist(String nickname, List<String> blackList) {
		StringBuilder sb = new StringBuilder();

		if (blackList.size() == 0) {
			sb.setLength(0);
		}
		if (blackList.size() == 1 ) {
			sb.setLength(0);
			sb.append(blackList.get(0));
		} else {
			sb.setLength(0);
			for (int i = 0; i < blackList.size(); i++) {
				sb.append(blackList.get(i));
				if (i < blackList.size() - 1) {
					sb.append(";");
				}
			}
		}

		try {
			String query = String.format("UPDATE users SET blacklist = '%s' WHERE nickname = '%s';", sb, nickname);
			PreparedStatement ps = connection.prepareStatement(query);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Загружает чёрный список из БД
	 */
	public static List<String> getBlacklist(String nickname) {
		String query = String.format("SELECT blacklist FROM users WHERE nickname = '%s'", nickname);
		String str = null;
		String temp = null;
		try {
			ResultSet rs = statement.executeQuery(query);
			if (rs.next()) {
				str = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (str != null) {
			if ("".equals(str)) {
				return Arrays.asList("");
			} else if (str.contains(";")) {
				String[] list = str.split(";");
				return Arrays.asList(list);
			} else {
				return Arrays.asList(str);
			}
		}
		return new ArrayList<>();
	}

	public static void saveHistory (String nickname, String history) {
		boolean isUpdated = false;

		String query = String.format("SELECT nickname FROM history WHERE nickname = '%s'", nickname);
		try {
			ResultSet rs = statement.executeQuery(query); // возвращает выборку через select

			if (rs.next()) {
				String updQuery = String.format("UPDATE history SET message_history = '%s' WHERE nickname = '%s';",
						history,
						nickname);
				PreparedStatement update = connection.prepareStatement(updQuery);
				update.executeUpdate();
				isUpdated = true;
			}

			if (!isUpdated) {
				String insertQuery = "INSERT INTO history (nickname, message_history) VALUES (?, ?);";
				PreparedStatement insert = connection.prepareStatement(insertQuery);
				insert.setString(1, nickname);
				insert.setString(2, history);
				insert.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
