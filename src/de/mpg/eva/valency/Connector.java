package de.mpg.eva.valency;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.mpg.eva.mapping.ForeignKeyRelation;
import de.mpg.eva.mapping.Mapping;
import de.mpg.eva.mapping.VocabularyMappingParser;

class Connector {

	private Connection conFileMaker;
	private Connection conMySQL;

	private Map<Integer, String> typeMap;

	public Connector() {

		createTypesMap();

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

		// connect to FileMaker and MySQL database
		try {

			String table = "Value2_examples";

			ResultSet rs = null;

			conFileMaker = DriverManager
					.getConnection(
							"jdbc:filemaker://127.0.0.1:2399/APiCS-unprotected.fp7",
							"admin", "");

//			conMySQL = DriverManager.getConnection(
//					"jdbc:mysql://139.18.2.158/apics", "idefix", "mpi2rdf");

			List<String> tables = getTableList(conFileMaker);
			getForeignKey(conFileMaker, table);
			
//			for(String s : tables) {
//				System.out.println(s);
//			}

			Statement st = conFileMaker.createStatement();
//			st.setMaxRows(2);
//			rs = st.executeQuery("SELECT COUNT(*) FROM " +table + " " );
//			rs = st.executeQuery("SELECT * FROM " +table + " f, Languages l where f.Feature_code like '%sovor%' and f.Language_ID=l.Language_ID " );


			ResultSet rs2 = null;
			DatabaseMetaData meta = conFileMaker.getMetaData();

			// The Oracle database stores its table names as Upper-Case,
			// if you pass a table name in lowercase characters, it will not
			// work.
			// MySQL database does not care if table name is
			// uppercase/lowercase.
			//
			
			

			List<String> columns = new ArrayList<String>();
//			rs2 = meta.getColumns(conFileMaker.getCatalog(), null, table, null);
			String[] types = {"TABLE"};
			rs2 = meta.getTables(conFileMaker.getCatalog(), null, "%", types );
			String tablesandcolumns = "";
			while (rs2.next()) {
				tablesandcolumns+="Table: " + rs2.getString("TABLE_NAME") + "\n";
				ResultSet rs3 = meta.getColumns(conFileMaker.getCatalog(), null, rs2.getString("TABLE_NAME"), null);
				int breakLines=0;
				while(rs3.next()) {
					breakLines++;
					if(!rs3.getString("COLUMN_NAME").startsWith("c_")) tablesandcolumns+=rs3.getString("COLUMN_NAME")+",";
					if(breakLines%10==0) tablesandcolumns+="\n";
				}
			}
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter("C:\\Users\\martin\\Documents\\llod\\APiCS\\apics-tables.txt"));
				out.write(tablesandcolumns);
				out.close();
				}
				catch (IOException e) {}
			
			
			
//			while (rs2.next()) {
//				
//				System.out.println(rs2.getString("TABLE_NAME"));
//				System.out.println(rs2.getString("COLUMN_NAME"));
//			//	columns.add(rs2.getString("COLUMN_NAME"));
//			}


//			while (rs.next()) {
//
//				System.out.println(rs.getString(1));
////				for(String column: columns){
////					System.out.println(column+": "+rs.getString(column));
////				}
//			}
	
		


			conFileMaker.close();
//			conMySQL.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void createTypesMap() {

		typeMap = new HashMap<Integer, String>();

		// Get all field in java.sql.Types
		Field[] fields = java.sql.Types.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			try {

				String name = fields[i].getName();
				Integer value = (Integer) fields[i].get(null);
				typeMap.put(value, name);

			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private void getForeignKey(Connection con, String table) {
		System.out.println("table:" + table);

		// The Oracle database stores its table names as Upper-Case,
		// if you pass a table name in lowercase characters, it will not
		// work.
		// MySQL database does not care if table name is
		// uppercase/lowercase.
		//
		try {
			ResultSet rs = null;
			DatabaseMetaData meta = con.getMetaData();
			// The Oracle database stores its table names as Upper-Case,
			// if you pass a table name in lowercase characters, it will not
			// work.
			// MySQL database does not care if table name is
			// uppercase/lowercase.
			//
			rs = meta.getExportedKeys(con.getCatalog(), null, table);
			while (rs.next()) {
				String fkTableName = rs.getString("FKTABLE_NAME");
				String fkColumnName = rs.getString("FKCOLUMN_NAME");
				int fkSequence = rs.getInt("KEY_SEQ");
				System.out.println("getExportedKeys(): fkTableName="
						+ fkTableName);
				System.out.println("getExportedKeys(): fkColumnName="
						+ fkColumnName);
				System.out.println("getExportedKeys(): fkSequence="
						+ fkSequence);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private List<String> getTableList(Connection con) {

		List<String> tables = new ArrayList<String>();

		DatabaseMetaData meta;
		try {

			meta = con.getMetaData();
			ResultSet rs = meta.getTables(null, null, "%", null);

			// get all tables
			while (rs.next())
				tables.add(rs.getString(3));

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return tables;

	}

	private String getSQLType(int type) {
		return typeMap.get(type);
	}
	
	/**
	 * Die Methode beschreibt das Vorgehen zur Erzeugung des Models.
	 * zuerst wird ein neues Jena Model erzeugt, sowie der Mappingfile eingelesen.
	 * Damit wird ein VocabularyMappingParser gestartet, von dem die Mappings geholt werden können.
	 * Diese sind in einer Map<String, Mapping>. Die Schlüssel sind Tabellen der DB, die jeweils ein Mapping besitzen.
	 * Vom Mapping können die anzufragenden Spalten mit .getColumns() als List<String> geholt werden.
	 * Außerdem enthält das Mapping den Primärschlüssel zum anfragen der id (getPrimary()) und eine Anzahl ForeignKeyRelations.
	 * Diese verweisen auf eine andere Tabelle (siehe inline kommentare)
	 * 
	 * @return
	 */
	
	public Model getValencyJenaModel() {

		//filepath should go in external file
		File mappingFile = new File("C:\\Users\\martin\\Documents\\llod\\vocabulary_mapping.xml");
		Model defModel = ModelFactory.createDefaultModel();
		VocabularyMappingParser parser = new VocabularyMappingParser(defModel, mappingFile);
		//keys of the map are db tables
		Map<String, Mapping> map = parser.getValencyMapping();
		FileMakerToJavaObjects fmToJava = new FileMakerToJavaObjects();
		for(String dbTable : map.keySet()) {
			Mapping tableMapping = map.get(dbTable);
			//this is obviously wrong, because there is more than one object in each table
			ValencyDbObject object = new ValencyDbObject();
			//get id from db by querying primaryKey
			String primaryKey = tableMapping.getPrimary();
			int id = 0;
			object.setId(id);
			
			for(String column : tableMapping.getColumns()) {
				//these are the columns of the table which are needed in the object
				String value = "query this from db";
				object.setFieldValue(column, value);
			}
			for(ForeignKeyRelation rel : tableMapping.getForeignRelations()) {
				//this is the name of another table. Primary is identical to table before, foreignKey is the value we want to get.
				String foreignTable = rel.getTable();
				String primary = rel.getPrimary();
				String foreignKey = rel.getForeign();
				//query the foreign table
				String value = "query the value of the foreignKey in table foreignTable where id=primary";
				object.setFieldValue(foreignKey, value);
			}
			fmToJava.addObject(object);
		}
		ValencyJenaModel modelMaker = new ValencyJenaModel(fmToJava, parser, defModel);
		defModel = modelMaker.fillJenaModel();
		return defModel;
	}

	public static void main(String[] args) {

		new Connector();

	}
}