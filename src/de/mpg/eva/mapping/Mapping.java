package de.mpg.eva.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

public class Mapping {

	private Model jenaModel;
	private Map<String, List<Property>> properties;
	private Map<String, List<ResourceProperty>> resources;
	private List<String> columns;
	private List<ForeignKeyRelation> foreignRelations;
	private String baseUri;
	private String primaryKey;
	
	public Mapping(Model model){
		this.jenaModel = model;
		this.properties = new TreeMap<String, List<Property>>();
		this.resources = new TreeMap<String, List<ResourceProperty>>();
		this.foreignRelations = new ArrayList<ForeignKeyRelation>();
		this.columns = new ArrayList<String>();
	}
	
		
	/**
	 * @param field
	 * @param property
	 * @param uri
	 */
	
	public void addResource(String field, String property , String uri){
		
		if(!this.columns.contains(field)) this.columns.add(field);
		
		String[] propertySplit = property.split(":"); 
		//if we know the namespace
		if(this.jenaModel.getNsPrefixMap().containsKey(propertySplit[0])) {
			//make new property
			Property prop = new PropertyImpl(this.jenaModel.getNsPrefixMap().get(propertySplit[0]), propertySplit[1] );
			ResourceProperty resProp = new ResourceProperty(prop, uri);
			if(this.resources.containsKey(field)) {
				List<ResourceProperty> resProps = this.resources.get(field);
				resProps.add(resProp);
			} else {
				List<ResourceProperty> resProps = new ArrayList<ResourceProperty>();
				resProps.add(resProp);
				this.resources.put(field, resProps);
			}
		} else {
			System.out.println("Namespace " + propertySplit[0] + " unknown, add as <namespace> element first.");
		}
	}
	
	/**
	 * @param field
	 * @param resource
	 */
	
	public void addProperty(String field, String resource){
		
		if(!this.columns.contains(field)) this.columns.add(field);
		
		String[] resourceSplit = resource.split(":"); 
		//if we know the namespace
		if(this.jenaModel.getNsPrefixMap().containsKey(resourceSplit[0])) {
			//make new property
			Property prop = new PropertyImpl(this.jenaModel.getNsPrefixMap().get(resourceSplit[0]), resourceSplit[1] );
			if(this.properties.containsKey(field)) {
				List<Property> props = this.properties.get(field);
				props.add(prop);
			} else {
				List<Property> props = new ArrayList<Property>();
				props.add(prop);
				this.properties.put(field, props);
			}
		} else {
			System.out.println("Namespace " + resourceSplit[0] + " unknown, add as <namespace> element first.");
		}
	}
	
	public void addForeignRelations(List<ForeignKeyRelation> relations) {
			this.foreignRelations.addAll(relations);
	}
	
	public void setUri(String uri) {
		this.baseUri = uri;
	}

	public String getUri() {
		return baseUri;
	}
	
	public void setPrimary(String primary) {
		this.primaryKey = primary;
	}
	
	public String getPrimary() {
		return this.primaryKey;
	}

	public Map<String, List<Property>> getProperties() {
		return properties;
	}

	public Map<String, List<ResourceProperty>> getResources() {
		return resources;
	}
	
	public List<String> getColumns() {
		return (this.columns.size()!=0) ? this.columns : null;
		
	}
	
	public List<ForeignKeyRelation> getForeignRelations() {
		return (this.foreignRelations.size()!=0) ? this.foreignRelations : null;
	}
	
	public String getResourcesString() {
		Set<Entry<String, List<ResourceProperty>>> entries=this.resources.entrySet();
		String out="";
		for(Entry<String, List<ResourceProperty>> entry : entries){
			for(ResourceProperty resProp: entry.getValue() ) {
				out+="DB field: " + entry.getKey() + " Property: " + resProp.getProperty().getURI() + " ResourceUri: " + resProp.getResourceUri() + "\n";
			}
		}
		return out;
	}
	
	public String getPropertiesString() {
		Set<Entry<String, List<Property>>> entries=this.properties.entrySet();
		String out="";
		for(Entry<String, List<Property>> entry : entries){
			for(Property prop: entry.getValue()) {
				out+="DB field: " + entry.getKey() + " Property: " + prop.getURI() + "\n";
			}
		}
		return out;
	}
	
}
