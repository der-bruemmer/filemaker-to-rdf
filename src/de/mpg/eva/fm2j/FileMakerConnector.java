package de.mpg.eva.fm2j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import com.hp.hpl.jena.graph.query.Query;

public class FileMakerConnector {

	private Connection conFileMaker = null;

	public FileMakerConnector(String filemakerURL, String filemakerUser,
			String filemakerPassword) {

		// register JDBC client driver
		try {

			Class.forName("com.filemaker.jdbc.Driver").newInstance();
			Class.forName("com.mysql.jdbc.Driver").newInstance();

		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			conFileMaker = DriverManager.getConnection(filemakerURL,
					filemakerUser, filemakerPassword);

			// conFileMaker.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public ResultSet getTable(String table, List<String> columns) {

		if (columns == null || columns.size() <= 0)
			return null;

		ResultSet rs = null;
		Statement st = null;
		String query = "SELECT ";

		// iterate over columns to build query
		// columns can contain a column with the tables name. discard it
		for (String column : columns)
			if (!column.equals(table))
				query += "\"" + column + "\"" + ",";

		// delete last ',' and add table
		query = query.substring(0, query.length() - 1);
		query += " FROM " + table;

		try {

			st = conFileMaker.createStatement();
			rs = st.executeQuery(query);

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return rs;

	}

	public ResultSet getTable(String query) {

		ResultSet rs = null;
		Statement st = null;

		try {

			st = conFileMaker.createStatement();
			rs = st.executeQuery(query);

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return rs;

	}

	public ResultSet getTable(String table, List<String> columns,
			String whereClause) {

		if (columns == null || columns.size() <= 0)
			return null;

		if (whereClause == null || whereClause.isEmpty())
			return getTable(table, columns);

		ResultSet rs = null;
		Statement st = null;
		String query = "SELECT ";

		// iterate over columns to build query
		// columns can contain a column with the tables name. discard it
		for (String column : columns)
			if (!column.equals(table))
				query += "\"" + column + "\"" + ",";

		// delete last ',' and add table
		query = query.substring(0, query.length() - 1);
		query += " FROM " + table + " " + whereClause;

		try {

			st = conFileMaker.createStatement();
			rs = st.executeQuery(query);

		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

		return rs;

	}

	public static void main(String[] args) throws SQLException {

		String filemakerURL = null;
		String user = null;
		String password = null;

		if (args.length > 0) {

			filemakerURL = args[0];
			user = args[1];
			password = args[2];

		} else {

			Properties properties = null;
			BufferedInputStream stream = null;
			try {

				properties = new Properties();
				stream = new BufferedInputStream(new FileInputStream(
						"config/filemaker.properties"));
				properties.load(stream);
				stream.close();

				filemakerURL = properties.getProperty("filemakerURL");
				user = properties.getProperty("filemakerUser");
				password = properties.getProperty("filemakerpassword");

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		FileMakerConnector connector = new FileMakerConnector(filemakerURL,
				user, password);
		String q = "SELECT \"Example_ID\" FROM Verb_Examples WHERE Verb_ID = 1";
		ResultSet rs = connector.getTable(q);
		while (rs.next()) {

//			System.out.println(rs.getString(0));

		}

	}
}
