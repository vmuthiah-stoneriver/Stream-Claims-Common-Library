package com.client.iip.integration.core.util;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.jboss.cache.Node;
import org.w3c.dom.Document;

import com.fiserv.isd.iip.core.cache.jbosscache.JBossCacheAccessor;
import com.fiserv.isd.iip.core.cache.jbosscache.JBossCacheManager;

public class XSDValidator {
	private static final String SCHEMA_CACHE = "client.integration.SchemaCache";
	public void validateSchema(String xml, String schemaName) throws Exception {      

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();     
		builderFactory.setNamespaceAware(true);
		
		DocumentBuilder parser = builderFactory.newDocumentBuilder();
		//Clean up non-parsable chars in the xml
		String regEx = "&#x[?:0[0-8BCEF]];";
		Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(xml);
		xml = m.replaceAll(""); 

		// parse the XML into a document object     
		Document document = parser.parse( IOUtils.toInputStream(xml, "UTF-8")); 
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);      
		// associate the schema factory with the resource resolver, which is responsible for resolving the imported XSD's     
		factory.setResourceResolver(new ResourceResolver());              
		// note that if your XML already declares the XSD to which it has to conform, then there's no need to create a validator from a Schema object     
		Schema schema = null;
		JBossCacheManager cacheMgr = JBossCacheAccessor.getJBossCache();
		Node <Object, Object> cacheNode = cacheMgr.getAppDataCache();
		Map<String, Schema> schemaMap = (Map<String, Schema>)cacheNode.get(SCHEMA_CACHE);
		if(schemaMap == null) {
			schemaMap = new ConcurrentHashMap<String, Schema>();
			cacheNode.put(SCHEMA_CACHE, schemaMap);
		}
		schema = schemaMap.get(schemaName);
		if(schema == null) {
			Source schemaFile = new StreamSource(getClass().getClassLoader().getResourceAsStream(schemaName));     
			schema = factory.newSchema(schemaFile);
			schemaMap.put(schemaName, schema);
		}
		Validator validator = schema.newValidator();     
		validator.validate(new DOMSource(document)); 
	}
	
	
	

}
