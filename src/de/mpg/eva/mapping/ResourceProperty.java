package de.mpg.eva.mapping;

/**
 * @author Martin Brümmer
 */

import com.hp.hpl.jena.rdf.model.Property;

public class ResourceProperty {
	
	private Property property;
	private String resourceUri;
	
	public ResourceProperty() {
	
	}
	
	public ResourceProperty(Property property, String resourceUri) {
		this.property = property;
		this.resourceUri = resourceUri;
	}
	public Property getProperty() {
		return property;
	}
	public void setProperty(Property property) {
		this.property = property;
	}
	public String getResourceUri() {
		return resourceUri;
	}
	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}
	
	
	
}
