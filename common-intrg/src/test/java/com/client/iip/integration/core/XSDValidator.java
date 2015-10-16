package com.client.iip.integration.core;


import java.io.InputStream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class XSDValidator {

	private final Logger logger = LoggerFactory.getLogger(getClass());
		
	public void validateSchema(String xml, String schemaName) throws Exception {
		logger.debug("Parsing schema : " + schemaName);
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
		InputStream inStream = this.getClass().getClassLoader().getResourceAsStream(schemaName);
		String xsdString = IOUtils.toString(inStream);
		xsdString = xsdString.replaceAll("element name=\"list\"", "element name=\"rootlist\"");
		Source schemaFile = new StreamSource(IOUtils.toInputStream(xsdString));
		Schema schema = factory.newSchema(schemaFile);
		Validator validator = schema.newValidator();     
		validator.validate(new DOMSource(document)); 
	}
	
	public static void main(String args[]) throws Exception{
		String payload="<reserveList>" +
					" <claimReserveTypeLossTypeNestedCodeLookUp> "+
					" <code class=\"string\">clm_exp</code> " +
					" <value>Claim Expense</value> " +    
					" <permission>1</permission> " +
					" <loseTypeColl class=\"list\"> " +
					" <reserveLoseTypeCodeLookUp> " +
					" <code class=\"string\">collsn</code> " +
					" <value>Collision</value> " +
					" <permission>1</permission> " +
					" <avgReserveDeftAvgAmt>0.00</avgReserveDeftAvgAmt> " +
					" <avgReserveDeftSpecAmt>0.00</avgReserveDeftSpecAmt> " +
					" <approved>false</approved> " +
					" <lossDefaultIndicator>false</lossDefaultIndicator> " +
					" </reserveLoseTypeCodeLookUp> "+
					" </loseTypeColl> " +
					" </claimReserveTypeLossTypeNestedCodeLookUp> " +
					" </reserveList> ";
		XSDValidator xsdval = new XSDValidator();
		xsdval.validateSchema(payload, "xsd/ClaimReserveTypeLossTypeNestedCodeLookup.xsd");
	}
	
	
}
