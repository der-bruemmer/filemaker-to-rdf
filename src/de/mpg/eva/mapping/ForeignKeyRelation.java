package de.mpg.eva.mapping;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Property;

public class ForeignKeyRelation {
	
	private String table;
	private String primary;
	private String foreign;
	private List<ResourceProperty> resources;
	private List<Property> props;
	
	public ForeignKeyRelation() {
		this.table = null;
		this.primary = null;
		this.foreign = null;
		this.resources = new ArrayList<ResourceProperty>();
		this.props = new ArrayList<Property>();
	}

	public String getTable() {
		return table;
	}
	
	public void setTable(String table) {
		this.table = table;
	}
	
	public String getPrimary() {
		return primary;
	}
	
	public void setPrimary(String primary) {
		this.primary = primary;
	}
	
	public String getForeign() {
		return foreign;
	}
	
	public void setForeign(String foreign) {
		this.foreign = foreign;
	}

	public List<ResourceProperty> getResources() {
		return resources;
	}
	
	public void addResource(ResourceProperty res) {
		this.resources.add(res);
	}

	public List<Property> getProperties() {
		return props;
	}
	
	public void addProperty(Property prop) {
		this.props.add(prop);
	}

}
