package com.client.iip.integration;

import java.io.File;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.XSDValidator;

public class XMLValidationTestCase  extends TestCase {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private String XMLFolder = "src\\test\\resources\\xml\\";
	
	private String XSDFolder = "xsd\\";
	
	private ResourceBundle xmlMap = null;

	/**
	 * Constructor for ApplyCreditsTestCase.
	 * 
	 * @param name
	 *            name.
	 */
	public XMLValidationTestCase(String name) {
		super(name);
		xmlMap = ResourceBundle.getBundle("iip-test-integration-config/xml-xsd-map");
		logger.debug("------------------------------ Process started ------------------------------");
		logger.debug("Inside XMLValidationTestCase constructor. Test case initiated.");
	}
	
	
	public void testInterfaceXMLs(){
		//Loop through the hashmap to test the XML's
		try{
			for(String key: xmlMap.keySet()){
				String xmlFileName = key;
				String xsdFileName = xmlMap.getString(xmlFileName);
				String strXML = FileUtils.readFileToString(new File(XMLFolder + xmlFileName));
				//xsd validator issue replace generic collections in root
				strXML = strXML.replaceAll("<list>", "<rootlist>");
				strXML = strXML.replaceAll("</list>", "</rootlist>");
				//strXML = strXML.replaceAll("&", "&amp;");
				//Now Validate the xml payload against the xsd
				logger.info("Validating XML Payload: " + xmlFileName + " against Schema: " + xsdFileName);
				XSDValidator xsdval = new XSDValidator();
				xsdval.validateSchema(strXML, XSDFolder + xmlMap.getString(xmlFileName));
				logger.info(xmlFileName + " Validated!!!");
			}
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail(e.toString());
		}		
		
	}
	

}
