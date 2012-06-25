package de.mpg.eva.valency;

/**
 * @author Martin Brümmer
 */

import java.util.HashMap;
import java.util.Map;

public class ValencyDbObject {
	
	private String id;	
	private Map<String, String> fieldValues;
	
	public ValencyDbObject() {
		this.fieldValues = new HashMap<String, String>();
	}
	
	public String getFieldValueByName(String fieldname) {
		if(this.fieldValues.containsKey(fieldname)) {
			return this.fieldValues.get(fieldname);
		} else return null;
	}
	
	public void setFieldValue(String fieldname, String value) {
		this.fieldValues.put(fieldname, value);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public boolean equals(ValencyDbObject object){
		return (this.id.equals(object.getId())) ? true : false;
	}
	
	public String toString(){
		String out=null;
		if(!this.fieldValues.isEmpty()) {
			for(String key : this.fieldValues.keySet()) {
				out+=key + ": " + this.fieldValues.get(key) + " " ;
			}
		}
		return out;
	}
}
