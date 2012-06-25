package de.mpg.eva.valency;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.mpg.eva.mapping.ForeignKeyRelation;
import de.mpg.eva.mapping.Mapping;
import de.mpg.eva.mapping.VocabularyMappingParser;
import de.mpg.eva.utils.IConstants;

class Connector {

	private VocabularyMappingParser parser = null;
	private Model defModel = null;

	private String filemakerURL = null;
	private String filemakerUser = null;
	private String fileMakerPassword = null;
	private String mappingFile = null;

	public Connector(String filemakerURL, String filemakerUser,
			String fileMakerPassword, String mappingFile) {
		System.out.println(mappingFile);
		this.filemakerURL = filemakerURL;
		this.filemakerUser = filemakerUser;
		this.fileMakerPassword = fileMakerPassword;
		this.mappingFile = mappingFile;

		defModel = ModelFactory.createDefaultModel();
		parser = new VocabularyMappingParser(defModel, new File(mappingFile));

		getValencyJenaModel();

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
	 */

	public Model getValencyJenaModel() {

		// filepath should go in external file

		// keys of the map are db tables
		Map<String, Mapping> map = parser.getValencyMapping();
		FileMakerToJavaObjects fmToJava = new FileMakerToJavaObjects();
		for (String dbTable : map.keySet()) {
			System.out.println("table: " + dbTable);
			Mapping tableMapping = map.get(dbTable);
			if (tableMapping == null) {
				System.out.println("Null");
			}
			// this is obviously wrong, because there is more than one object in
			// each table
			ValencyDbObject object = new ValencyDbObject();
			// get id from db by querying primaryKey
			String primaryKey = tableMapping.getPrimary();
			System.out.println("PR :" + primaryKey + "\n");
			int id = 0;
			object.setId(id);

			for (String column : tableMapping.getColumns()) {
				// these are the columns of the table which are needed in the
				// object
				String value = "query this from db";
				object.setFieldValue(column, value);
			}
			for (ForeignKeyRelation rel : tableMapping.getForeignRelations()) {
				// this is the name of another table. Primary is identical to
				// table before, foreignKey is the value we want to get.
				String foreignTable = rel.getTable();
				String primary = rel.getPrimary();
				String foreignKey = rel.getForeign();
				// query the foreign table
				String value = "query the value of the foreignKey in table foreignTable where id=primary";
				object.setFieldValue(foreignKey, value);
			}
			fmToJava.addObject(object);
		}
		ValencyJenaModel modelMaker = new ValencyJenaModel(fmToJava, parser,
				defModel);
		defModel = modelMaker.fillJenaModel();
		return defModel;
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