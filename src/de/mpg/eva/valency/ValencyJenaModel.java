package de.mpg.eva.valency;

/**
 * @author Martin Br�mmer
 */

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import de.mpg.eva.mapping.ForeignKeyRelation;
import de.mpg.eva.mapping.Mapping;
import de.mpg.eva.mapping.ResourceProperty;
import de.mpg.eva.mapping.VocabularyMappingParser;

public class ValencyJenaModel {
	
	private Model jenaModel;
	private FileMakerToJavaObjects objects;
	private Map<String, Mapping> valencyMapping;
	
	public ValencyJenaModel(FileMakerToJavaObjects objects, VocabularyMappingParser parser, Model model) {
		this.jenaModel = model;
		this.valencyMapping = parser.getValencyMapping();
		this.objects = objects;
	}
	
	public Model fillJenaModel() {
		Map<String,Map<String, ValencyDbObject>> objects = this.objects.getAllObjects();
		for(String table : objects.keySet()) {
			Mapping langMapping = this.valencyMapping.get(table);
			for(ValencyDbObject obj : objects.get(table).values()) {
				String resourceUri = makeUri(langMapping.getUri(), obj.getId());
				Resource langResource = jenaModel.createResource(resourceUri);
				this.addPropertiesAndResources(langResource, langMapping, obj);	
			}
		}
		
		
		return this.jenaModel;
	}
	
	private void addPropertiesAndResources(Resource res, Mapping map, ValencyDbObject obj) {
		for(String field : map.getResources().keySet()) {
			for(ResourceProperty resProp : map.getResources().get(field)) {
				String uri = resProp.getResourceUri();
				if(containsFieldValue(uri, field)) {
					if(obj.getFieldValueByName(field)!=null) {
						res.addProperty(resProp.getProperty(), makeUri(resProp.getResourceUri(),obj.getFieldValueByName(field)));
					} 
					else System.out.println("Field not found: " + field);
				}
				else res.addProperty(resProp.getProperty(),resProp.getResourceUri());
			}
			for(Property prop : map.getProperties().get(field)) {
				if(obj.getFieldValueByName(field)!=null) {
					res.addLiteral(prop, obj.getFieldValueByName(field));
				}
				else System.out.println("Field not found: " + field);
			}
		}
		for(ForeignKeyRelation rel : map.getForeignRelations()) {
			String field = rel.getForeign();
			for(ResourceProperty resProp : rel.getResources()) {
				String uri = resProp.getResourceUri();
				if(containsFieldValue(uri, field)) {
					if(obj.getFieldValueByName(field)!=null) {
						res.addProperty(resProp.getProperty(), makeUri(resProp.getResourceUri(),obj.getFieldValueByName(field)));
					} 
					else System.out.println("Field not found: " + field);
				}
				else res.addProperty(resProp.getProperty(),resProp.getResourceUri());
			}
			for(Property prop : rel.getProperties()) {
				if(obj.getFieldValueByName(field)!=null) {
					res.addLiteral(prop, obj.getFieldValueByName(field));
				}
				else System.out.println("Field not found: " + field);
			}
		}
	}
	
	private boolean containsFieldValue(String uri, String field) {
		if(uri.lastIndexOf("$"+field)!=-1) return true;
		else return false;
	}
	
	private String makeUri(String baseUri, String id) {
		String uri = "";
		uri = baseUri.substring(0, baseUri.lastIndexOf("$")) + id;
		return uri;
	}
}
