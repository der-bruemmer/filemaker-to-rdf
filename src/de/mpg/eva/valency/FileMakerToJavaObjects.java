package de.mpg.eva.valency;

import java.util.HashMap;
import java.util.Map;

/**
 * This Class serves to build Maps of Java Objects from the Valency FileMaker Database
 * 
 * Because of the internal structure of the database, it would be best to create Languages first,
 * then Meanings (every Meaning has a Language field), 
 * then Examples (every Example has a Language field),
 * then Verbs (every Verb has a List of Examples and Meanings and a Language field)
 * 
 * @author Martin Brümmer
 */

public class FileMakerToJavaObjects {

	private Map<Integer, ValencyDbObject> objects;
	
	public FileMakerToJavaObjects() {
		this.objects = new HashMap<Integer,ValencyDbObject>();
	}
	
	public void addObject(ValencyDbObject object) {
		this.objects.put(object.getId(), object);
	}
	
	public ValencyDbObject getObjectById(int id) {
		return this.objects.get(id);
	}
	
	public Map<Integer, ValencyDbObject> getObjectMap() {
		return this.objects;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
