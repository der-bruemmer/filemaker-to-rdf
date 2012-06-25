package de.mpg.eva.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;

public class VocabularyMappingParser {
	
	private Model jenaModel;
	private Map<String, Mapping> valencyMapping;
	private File mappingFile;
	
	public VocabularyMappingParser(Model model, File mappingFile) {
		this.jenaModel = model;
		this.valencyMapping = new HashMap<String, Mapping>();
		this.mappingFile = mappingFile;
		this.setNameSpaces();
	}
	
	public void setNameSpaces() {
		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		    domFactory.setNamespaceAware(true);
		    DocumentBuilder builder = domFactory.newDocumentBuilder();
		    Document doc = builder.parse(this.mappingFile);
		    NodeList nameSpaceNodes = getNodesByPath(doc, "//valency/namespaces/namespace");
		    for(int nsNumber=0; nsNumber<nameSpaceNodes.getLength(); nsNumber++) {
		    	Node nameSpaceNode = nameSpaceNodes.item(nsNumber);
		    	if(nameSpaceNode.getNodeType() == Node.ELEMENT_NODE) {
		    		//splitting the namespace nodes content and adding prefix and uri to jena model
		    		String[] nsVal=nameSpaceNode.getFirstChild().getTextContent().trim().split("=");
		    		this.jenaModel.setNsPrefix(nsVal[0], nsVal[1]);
		    	}
		    }		    
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
	}

	public Map<String, Mapping> getValencyMapping() {
		
		if(this.valencyMapping.isEmpty()) {
			try {
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			    domFactory.setNamespaceAware(true);
			    DocumentBuilder builder = domFactory.newDocumentBuilder();
			    Document doc = builder.parse(this.mappingFile);
			    NodeList valencyNodes = this.getNodesByPath(doc, "//valency//table");
			  
			    //for every node of the corresponding mapping
			    for(int nNumber=0; nNumber<valencyNodes.getLength(); nNumber++) {
			    	Node valNode = valencyNodes.item(nNumber);
			    	//if the node is an element and describes a table
			    	if(valNode.getNodeType() == Node.ELEMENT_NODE) {
			    		//make a new mapping for the table
			    		Mapping objMapping = new Mapping(this.jenaModel);
			    		//traverse recursively from the first childnode of the table element to get the mapping  		
			    		this.traverseDomFromNode(valNode, objMapping);
			    		//add mapping to mapping map (wtf am i doing?!)
			    		valencyMapping.put(objMapping.getTable(), objMapping);
			    	}
			    }
		    } catch(Exception e) {
		    	e.printStackTrace();
		    }
		}
		return valencyMapping;
	}
	
	private NodeList getNodesByPath(Document doc, String xpath) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
	    XPath xpathObject = factory.newXPath();
	    XPathExpression expr = xpathObject.compile(xpath);
	    Object result = expr.evaluate(doc, XPathConstants.NODESET);
	    NodeList nodes = (NodeList) result;
	    return nodes;
	}
	
	private void traverseDomFromNode(Node parent, Mapping mapping) {
		//as long as there are nodes to parse
		if(parent.hasChildNodes()) {
			NodeList childNodes = parent.getChildNodes();
			//uri of the resource is set here
			if(parent.getNodeName().equals("table")) {
				mapping.setTable(parent.getChildNodes().item(1).getNodeName());
			}
			else if(parent.getNodeName().equals("URI")) {
				mapping.setUri(parent.getFirstChild().getTextContent().trim());
			}
			else if(parent.getNodeName().equals("primary")) {
				//name of the nodes child is primary key
				mapping.setPrimary(parent.getChildNodes().item(1).getNodeName());
			}
			//resources are added into a Map<String, ResourceProperty>
			//ResourceProperty is initialized in mapping.addResource method
			//it is simultaniously added to mapping.columns
			else if(parent.getNodeName().equals("resource") && 
					!parent.getParentNode().getParentNode().getNodeName().equals("foreign")) {
				String prop=parent.getChildNodes().item(1).getTextContent().trim();
				String uri=parent.getChildNodes().item(3).getTextContent().trim();
				mapping.addResource(parent.getParentNode().getNodeName(), prop, uri);
			}
			//properties are added to a Map<String, Property>
			//the first string contains the db field, the second the property 
			//Property is initialized in mapping.addProperty
			else if(parent.getNodeName().equals("property") &&
					!parent.getParentNode().getNodeName().equals("resource")) {
				mapping.addProperty(parent.getParentNode().getNodeName(), parent.getFirstChild().getTextContent().trim());
			} 
			else if(parent.getNodeName().equals("foreign")) {
				mapping.addForeignRelations(this.addForeigns(parent));
			}
			//recursive traverse of dom
			for(int nodeNumber=0; nodeNumber<childNodes.getLength(); nodeNumber++) {
				traverseDomFromNode(childNodes.item(nodeNumber), mapping);
			}
		} 
	}
	
	/**
	 * Add all foreign key relations with included properties and resources for a certain node
	 * @param parent
	 * @return
	 */
	
	private List<ForeignKeyRelation> addForeigns(Node parent) {
		List<ForeignKeyRelation> foreigns = new ArrayList<ForeignKeyRelation>();
		
		//start with childnodes of <foreign>
		for(int fNodeNumber = 0; fNodeNumber<parent.getChildNodes().getLength(); fNodeNumber++) {
			//new relation for every tuple of foreign table, primary, foreignkey
			Node fNode = parent.getChildNodes().item(fNodeNumber);
			if(fNode.getNodeType() == Node.ELEMENT_NODE) {
				ForeignKeyRelation rel = new ForeignKeyRelation();
				rel.setTable(parent.getAttributes().getNamedItem("table").getTextContent());
				//<column> element with attributes primary and foreign
				rel.setPrimary(fNode.getAttributes().getNamedItem("primary").getTextContent());
				rel.setForeign(fNode.getAttributes().getNamedItem("foreign").getTextContent());
				//these are resource or property nodes
				for (Node resNode = fNode.getFirstChild(); resNode != null; resNode = resNode.getNextSibling()) {
					//make a new resource and add to foreign
					if(resNode.getNodeName().equals("resource")) {
						String prop=resNode.getChildNodes().item(1).getTextContent().trim();
						String[] propertySplit = prop.split(":"); 
						String uri=resNode.getChildNodes().item(3).getTextContent().trim();
						if(this.jenaModel.getNsPrefixMap().containsKey(propertySplit[0])) {
							ResourceProperty res = new ResourceProperty();
							Property property = new PropertyImpl(this.jenaModel.getNsPrefixMap().get(propertySplit[0]), propertySplit[1] );
							ResourceProperty resProp = new ResourceProperty(property, uri);
							rel.addResource(resProp);
						}	
					//make a new property and add to foreign
					} else if(resNode.getNodeName().equals("property")) {
						String prop=resNode.getChildNodes().item(1).getTextContent().trim();
						String[] propertySplit = prop.split(":");
						if(this.jenaModel.getNsPrefixMap().containsKey(propertySplit[0])) {
							Property property = new PropertyImpl(this.jenaModel.getNsPrefixMap().get(propertySplit[0]), propertySplit[1] );
							rel.addProperty(property);
						}
					}
				}
				foreigns.add(rel);
			}
			
		}
		return foreigns;
	}
	

	public void setValencyMapping(Map<String, Mapping> valencyMapping) {
		this.valencyMapping = valencyMapping;
	}

	public File getMappingFile() {
		return mappingFile;
	}

	public void setMappingFile(File mappingFile) {
		this.mappingFile = mappingFile;
	}
	
	public static void main(String[] args) {
		
		File mappingFile = new File("C:\\Users\\martin\\Documents\\llod\\vocabulary_mapping.xml");
		Model defModel = ModelFactory.createDefaultModel();
		VocabularyMappingParser parser = new VocabularyMappingParser(defModel, mappingFile);
		for(Mapping map : parser.getValencyMapping().values()) {
			System.out.println(map.getUri());
			System.out.println(map.getTable());
			System.out.println(map.getResourcesString()+"\n");
			System.out.println(map.getPropertiesString()+"\n");
//			for(ForeignKeyRelation f : map.getForeignRelations()) {
//				System.out.println(f.getTable());
//				System.out.println(f.getPrimary());
//				System.out.println(f.getForeign());
//			}
//			for(String s : map.getColumns()) {
//				System.out.println(s);
//			}
		}
	}
	
	
}
