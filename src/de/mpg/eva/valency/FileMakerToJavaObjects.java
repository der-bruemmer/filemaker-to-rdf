package de.mpg.eva.valency;

import java.util.HashMap;
import java.util.Map;

/**
 * This Class serves to build Maps of Java Objects from the Valency FileMaker Database
 * 
 * @author Martin Brümmer
 */

public class FileMakerToJavaObjects {

	private Map<String,Map<String, ValencyDbObject>> objects;
	
	public FileMakerToJavaObjects() {
		this.objects = new HashMap<String,Map<String, ValencyDbObject>>();
	}
	
	public void addObject(ValencyDbObject object, String table) {
		if(this.objects.containsKey(table)) {
			Map<String, ValencyDbObject> map = this.objects.get(table);
			map.put(object.getId(), object);
		} else {
			Map<String, ValencyDbObject> map = new HashMap<String, ValencyDbObject>();
			map.put(object.getId(), object);
			this.objects.put(table, map);
		}
		
	}
	
	public ValencyDbObject getObjectById(String id, String table) {
		return this.objects.get(table).get(id);
	}
	
	public Map<String, ValencyDbObject> getObjectMap(String table) {
		return this.objects.get(table);
	}
	
	public Map<String,Map<String, ValencyDbObject>> getAllObjects() {
		return this.objects;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
