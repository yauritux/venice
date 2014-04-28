package com.gdn.venice.hssf;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MappingReader {

	/*
	 * Melakukan mapping order Properties
	 */
	private ArrayList<String> mapResult=new ArrayList<String>();
	
	
	private ArrayList<HashMap<String, Class>> mappingResult = new ArrayList<HashMap<String,Class>>();
	
	/*
	 * Keeps track of the column being processed
	 */
	private Integer resultColumn = 1;
	
	public void readMapOrder(String fileName) throws Exception {
		
		try {
			Document doc = getDocument(fileName);
			Element root = doc.getDocumentElement();
			Node element = root.getFirstChild();
			resultColumn = 1;
			while (element != null) {
				if (element.getNodeName().toString().equalsIgnoreCase("mapping-element")) {
					this.mapResult.add("set" +element.getTextContent());	
				}
				element = element.getNextSibling();
				resultColumn++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Column mapping failed at column:" + resultColumn);
		}
	}
	
	public ArrayList<HashMap<String, Class>> readMappingOrder(String fileName) {
		try {
			Document doc = getDocument(fileName);
			
			ClassLoader classLoader = MappingReader.class.getClassLoader();
			
			NodeList listOfElement = doc.getElementsByTagName("data");
			for (int i = 0; i < listOfElement.getLength(); i++) {
				Element data = (Element) listOfElement.item(i);
				String dataName = data.getElementsByTagName("name").item(0).getTextContent();
				Class dataType = classLoader.loadClass(data.getElementsByTagName("type").item(0).getTextContent());
				
				HashMap<String, Class> mappingElement = new HashMap<String, Class>();
				mappingElement.put(dataName, dataType);
				this.mappingResult.add(mappingElement);
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.mappingResult.clear();
		}
		
		return this.mappingResult;
	}
	
	private static Document getDocument(String name) throws Exception {
		try {
			
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setIgnoringComments(true);
				factory.setIgnoringElementContentWhitespace(true);
				factory.setValidating(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				return builder.parse(new InputSource(name));
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("getDocument failed. Original exception was:" + e.getMessage());
		}
	}
	
	public ArrayList<String> getMapResult() {
		return mapResult;
	}

	public void setMapResult(ArrayList<String> mapResult) {
		
		this.mapResult = mapResult;
	}
	
	/**
	 * @return the result column being processed
	 */
	public Integer getResultColumn() {
		return resultColumn;
	}
	
	public ArrayList<HashMap<String, Class>> getMappingResult() {
		return mappingResult;
	}

}
