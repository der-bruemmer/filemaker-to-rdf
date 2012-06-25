package de.mpg.eva.valency;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.mpg.eva.fm2j.FileMakerConnector;
import de.mpg.eva.mapping.ForeignKeyRelation;
import de.mpg.eva.mapping.Mapping;
import de.mpg.eva.mapping.VocabularyMappingParser;
import de.mpg.eva.utils.IConstants;

public class Connector {

	private FileMakerConnector fileMakerConnector = null;
	private VocabularyMappingParser parser = null;
	private Model defModel = null;

	private String filemakerURL = null;
	private String filemakerUser = null;
	private String fileMakerPassword = null;
	private String mappingFile = null;

	public Connector(String filemakerURL, String filemakerUser,
			String filemakerPassword, String mappingFile) {

		this.filemakerURL = filemakerURL;
		this.filemakerUser = filemakerUser;
		this.fileMakerPassword = filemakerPassword;
		this.mappingFile = mappingFile;

		fileMakerConnector = new FileMakerConnector(filemakerURL,
				filemakerUser, filemakerPassword);
		defModel = ModelFactory.createDefaultModel();
		parser = new VocabularyMappingParser(defModel, new File(mappingFile));

		try {
			getValencyJenaModel();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("finished");
	}

	/**
	 * Die Methode beschreibt das Vorgehen zur Erzeugung des Models. zuerst wird
	 * ein neues Jena Model erzeugt, sowie der Mappingfile eingelesen. Damit
	 * wird ein VocabularyMappingParser gestartet, von dem die Mappings geholt
	 * werden k�nnen. Diese sind in einer Map<String, Mapping>. Die Schl�ssel
	 * sind Tabellen der DB, die jeweils ein Mapping besitzen. Vom Mapping
	 * k�nnen die anzufragenden Spalten mit .getColumns() als List<String>
	 * geholt werden. Au�erdem enth�lt das Mapping den Prim�rschl�ssel zum
	 * anfragen der id (getPrimary()) und eine Anzahl ForeignKeyRelations. Diese
	 * verweisen auf eine andere Tabelle (siehe inline kommentare)
	 * 
	 * @return
	 * @throws SQLException
	 */

	public Model getValencyJenaModel() throws SQLException {

		// filepath should go in external file

		// keys of the map are db tables
		Map<String, Mapping> map = parser.getValencyMapping();
		FileMakerToJavaObjects fmToJava = new FileMakerToJavaObjects();
		List<String> columns;

		ResultSet rsPrim = null;

		for (String dbTable : map.keySet()) {

			Mapping tableMapping = map.get(dbTable);
			ValencyDbObject object = null;

			columns = new ArrayList<String>(tableMapping.getColumns());
			columns.add(tableMapping.getPrimary());

			rsPrim = fileMakerConnector.getTable(dbTable, columns);
			while (rsPrim.next()) {

				ResultSet rsForeign = null;

				// this is obviously wrong, because there is more than one
				// object in
				// each table
				object = new ValencyDbObject();
				// get id from db by querying primaryKey
				String id = rsPrim.getString(tableMapping.getPrimary());
				object.setId(id);

				for (String column : tableMapping.getColumns()) {
					// these are the columns of the table which are needed in
					// the
					// object

					// String value = "query this from db";
					if (!column.equals(dbTable))
						object.setFieldValue(column, rsPrim.getString(column));
				}
				if (tableMapping.getForeignRelations() != null) {
					for (ForeignKeyRelation rel : tableMapping
							.getForeignRelations()) {

						String foreignTable = rel.getTable();
						String primary = rel.getPrimary();
						String foreignKey = rel.getForeign();

						rsForeign = fileMakerConnector.getTable("SELECT "
								+ "\"" + foreignKey + "\"" + " FROM "
								+ foreignTable + " WHERE " + primary + " = "
								+ id);

						while (rsForeign.next()) {
							object.setFieldValue(foreignKey,
									rsForeign.getString(foreignKey));
						}

						// this is the name of another table. Primary is
						// identical
						// to
						// table before, foreignKey is the value we want to get.
						// String foreignTable = rel.getTable();
						// String primary = rel.getPrimary();
						// String foreignKey = rel.getForeign();
						// query the foreign table
						// System.out.println(foreignTable);
						// System.out.println(primary);
						// System.out.println(foreignKey);
						// String value =
						// "query the value of the foreignKey in table foreignTable where id=primary";
						// object.setFieldValue(foreignKey,
						// rsPrim.getString(foreignKey));
					}
				}
			}
			fmToJava.addObject(object, dbTable);
		}
		ValencyJenaModel modelMaker = new ValencyJenaModel(fmToJava, parser,
				defModel);
		defModel = modelMaker.fillJenaModel();
		return defModel;
		// return null;
	}

	public static void main(String[] args) {

		String filemakerURL = null;
		String filemakerUser = null;
		String fileMakerPassword = null;
		String mappingFile = null;

		if (args.length > 0) {

			filemakerURL = args[0];
			filemakerUser = args[1];
			fileMakerPassword = args[2];
			mappingFile = args[3];

		} else {

			Properties properties = null;
			BufferedInputStream stream = null;
			try {

				properties = new Properties();
				stream = new BufferedInputStream(new FileInputStream(
						"config/filemaker.properties"));
				properties.load(stream);
				stream.close();

				filemakerURL = properties.getProperty(IConstants.FM_URL);
				filemakerUser = properties.getProperty(IConstants.FM_USER);
				fileMakerPassword = properties.getProperty(IConstants.FM_PASS);
				mappingFile = properties.getProperty(IConstants.MAPPING_FILE);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		Connector connector = new Connector(filemakerURL, filemakerUser,
				fileMakerPassword, mappingFile);

	}
}