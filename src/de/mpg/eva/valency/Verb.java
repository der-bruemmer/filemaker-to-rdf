//package de.mpg.eva.valency;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class Verb implements ValencyDbObject {
//	
//	private int id;	
//	private Map<String, String> fieldValues;
//	
//	
//	public Verb(){
//		this.fieldValues = new HashMap<String, String>();
//	}
//	
//	public String getFieldValueByName(String fieldname) {
//		if(this.fieldValues.containsKey(fieldname)) {
//			return this.fieldValues.get(fieldname);
//		} else return null;
//	}
//	
//	public void setFieldValue(String fieldname, String value) {
//		this.fieldValues.put(fieldname, value);
//	}
//
//	public int getId() {
//		return id;
//	}
//	
//	public void setId(int id) {
//		this.id = id;
//	}
//	
//	public boolean equals(Verb verb){
//		return (this.id==verb.getId()) ? true : false;
//	}
//	
//	public String toString(){
//		String out=null;
//		if(!this.fieldValues.isEmpty()) {
//			for(String key : this.fieldValues.keySet()) {
//				out+=key + ": " + this.fieldValues.get(key) + " " ;
//			}
//		}
//		return out;
//	}
//}
