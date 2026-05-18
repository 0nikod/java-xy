package com.campus.secondhand.util;

import com.campus.secondhand.config.AppConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DBUtil {

	private DBUtil() {
	}

	public static void ensureDatabaseReady() throws IOException, SQLException {
		Path databasePath = AppConfig.resolveDatabasePath();
		Path parent = databasePath.getParent();
		if (parent != null) {
			Files.createDirectories(parent);
		}
		try (Connection ignored = getConnection()) {
			// Opening the connection is enough to create the SQLite file.
		}
	}

	public static Connection getConnection() throws SQLException {
		Connection connection = DriverManager.getConnection(AppConfig.getDatabaseUrl());
		try (Statement statement = connection.createStatement()) {
			statement.execute("PRAGMA foreign_keys = ON");
		}
		return connection;
	}
}
