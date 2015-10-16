package com.client.iip.integration.core.util;

//w3c imports
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.xpath.XPathAPI;
import org.exolab.castor.xml.Marshaller;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.freedomgroup.common.exception.TFGNestingException;
import com.freedomgroup.common.xml.TFGXMLObject;
/**
 * Created on Aug 23, 2005
 * @author Guru Radhakrishnan
 * <br>StoneRiver
 * <br>XML Utility that helps in working with xmls
 * 
 */
public class XMLUtility extends TFGXMLObject{
	
private static Logger logger = Logger.getLogger(XMLUtility.class.getName());

	private XMLUtility(){
	}
	
	/**
	 * <br>Fetches the Value of the Node based on the XPATH Expression and XML String
	 * This method uses TFGXMLObject Wrapper to parse the XML. Converts the String to ByteArrayInputStream 
	 * for XML Processing
	 * @param xmlIn java.lang.String
	 * @param xPath java.lang.String
	 * @return java.lang.String
	 * @throws TFGNestingException
	 */
	
	
	public String fetchNodeValue(String xmlIn, String xPath) throws TFGNestingException{
		Document doc = null;
		Node eventDescr = null;
		String eventString = null;
		try{
			doc = XMLStringToDOM(xmlIn);
			eventDescr = XPathAPI.selectSingleNode(doc, xPath);
			eventString = eventDescr.getFirstChild().getNodeValue();
		}
		catch(javax.xml.transform.TransformerException e1){
			logger.error("TransformationException", e1);
			doc = null;
			eventDescr = null;
			eventString = null;
		}
		return eventString;
	}
	
	/**
	 * <br>This API takes the XPath Query String and returns value of selectedNode
	 * @param xPathString - java.lang.String
	 * @param _doc - org.w3c.dom.Document
	 * @return java.lang.String
	 * @throws TFGNestingException
	 */
	public static String fetchNodeValue(String xPath, Document _doc) throws TFGNestingException{
		Node eventDescr = null;
		String eventString = null;
		try{
			eventDescr = XPathAPI.selectSingleNode(_doc, xPath);
			eventString = eventDescr.getFirstChild().getNodeValue();
		}
		catch(javax.xml.transform.TransformerException e1){
			logger.error("TransformationException", e1);
			eventDescr = null;
			eventString = null;
			throw new TFGNestingException("TransformationException", e1);
		}
		return eventString;
	}		
	
	/**
	 * <br>Returns XML Document Object from the XML String
	 * @param xmlIn java.lang.String
	 * @return org.w3c.dom.Document
	 * @exception TFGNestingException
	 */
	public Document fetchDocument(String xmlIn) throws TFGNestingException{
			Document doc = null;
			InputStream instream = new ByteArrayInputStream(xmlIn.getBytes());
			doc = parse(instream);
			return doc;		
	}

	/**
	 * Converts Node Element into xml String
	 * @param node
	 * @return
	 */	
	public String convertNodeToString(Node node) {
		  StringWriter sw = new StringWriter();
		  try {
		    Transformer t = TransformerFactory.newInstance().newTransformer();
		    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    t.transform(new DOMSource(node), new StreamResult(sw));
		  } catch (TransformerException te) {
		    System.out.println("nodeToString Transformer Exception");
		  }
		  return sw.toString();
	}	
	
	/**
	 * <br>Returns NodeList from the XPATH Xpression. Input is XML String
	 * @param xmlIn java.lang.String
	 * @param xPath java.lang.String
	 * @return org.w3c.dom.NodeList
	 * @exception TFGNestingException
	 */
	
	public NodeList fetchNodeList(String xmlIn, String xPath) throws TFGNestingException{
		NodeList nList = null;
		Document doc = null;
		try{
			doc = fetchDocument(xmlIn);
			nList = XPathAPI.selectNodeList(doc, xPath);
			
		}
		catch(javax.xml.transform.TransformerException e1){
			logger.error("TransformationException", e1);
			doc = null;
			nList = null;
		}
		return nList;
			
	}
	
	/**
	 * <br>Create an XML String based on the Hashmap Name value Pairs -- Note this doesn't include Prolog
	 * @param map java.util.HashMap
	 * @return java.lang.String
	 * @exception TFGNestingException
	 */
	 
	 public static String composeXML(HashMap map) throws TFGNestingException{
	 	StringBuffer xmlOut = new StringBuffer();
	 	Iterator iterator = map.keySet().iterator();
	 		 	
	 	while(iterator.hasNext()){
	 			String nameKey = (String)iterator.next();
	 			String value = (String)map.get(nameKey)==null?"":(String)map.get(nameKey);
				xmlOut.append("<" + nameKey + ">");
				xmlOut.append(value);
				xmlOut.append("</" + nameKey + ">\n");
	 		}
	 	return xmlOut.toString();
	 	
	}
	
	/**
	 * <br>Translates DO to XML using the castor Marshalling utility. The API Parses the get and
	 * setter methods and constructs the XML Tags and values for them.
	 * @param doObject java.lang.Object
	 * @return java.lang.String XMLString represntation of the DO
	 */
	
	public static String translateDOtoXML(Object doObject) 
	throws org.exolab.castor.xml.ValidationException, org.exolab.castor.xml.MarshalException,	IOException {
				logger.debug("BEGIN: translateDOtoXML"); 
				StringWriter sw = new StringWriter();
				Marshaller marshaller = new Marshaller(sw);
				marshaller.setSuppressNamespaces(true);
				marshaller.marshal(doObject);
				String xmlStr = sw.toString();
				logger.debug("END: translateDOtoXML");
				return xmlStr;
		}	
	
	/*
	 * Method that fetches the Singleton instance
	 */
	public static XMLUtility  getInstance(){
		return new XMLUtility();	
	}
	
	/*public static void main(String args[]){
		
	try{
			XMLUtility xmlutil = new XMLUtility();
			String inXML = "<?xml version='1.0' ?><definitions name='FABWSImportManagerService' targetNamespace='http://service.externalinterface.afi.com.wsdl/FABWSImportManagerService/' xmlns='http://schemas.xmlsoap.org/wsdl/' xmlns:binding='http://service.externalinterface.afi.com.wsdl/FABWSImportManagerBinding/' xmlns:soap='http://schemas.xmlsoap.org/wsdl/soap/' xmlns:tns='http://service.externalinterface.afi.com.wsdl/FABWSImportManagerService/'><import location='FABWSImportManagerBinding.wsdl' namespace='http://service.externalinterface.afi.com.wsdl/FABWSImportManagerBinding/'/><service name='FABWSImportManagerService'><port binding='binding:FABWSImportManagerBinding' name='FABWSImportManagerPort'><soap:address location='http://localhost:9080/AFIWeb/servlet/rpcrouter'/></port></service></definitions>";
			System.out.println("OUTPUT::: " + xmlutil.fetchNodeValue(inXML,"//*[local-name() = 'definitions']/service/port/soap:address/@location"));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}*/
	
	public static String encodeXML(String inXML){
		Pattern p1 = Pattern.compile("<");
		Matcher m1 = p1.matcher(inXML); // get a matcher object
		StringBuffer sb1 = new StringBuffer();
		while (m1.find()) {
		  m1.appendReplacement(sb1, "&lt;");
		}
		inXML =	m1.appendTail(sb1).toString();
			
		Pattern p2 = Pattern.compile(">");
		Matcher m2 = p2.matcher(inXML); // get a matcher object
		StringBuffer sb2 = new StringBuffer();
		while (m2.find()) {
		  m2.appendReplacement(sb2, "&gt;");
		}
		String outXML =	m2.appendTail(sb2).toString();			
				
		return outXML;
	}
	
	public static String decodeXML(String inXML){
		Pattern p1 = Pattern.compile("&lt;");
		Matcher m1 = p1.matcher(inXML); // get a matcher object
		StringBuffer sb1 = new StringBuffer();
		while (m1.find()) {
		  m1.appendReplacement(sb1, "<");
		}
		inXML =	m1.appendTail(sb1).toString();
			
		Pattern p2 = Pattern.compile("&gt;");
		Matcher m2 = p2.matcher(inXML); // get a matcher object
		StringBuffer sb2 = new StringBuffer();
		while (m2.find()) {
		  m2.appendReplacement(sb2, ">");
		}
		String outXML =	m2.appendTail(sb2).toString();			
				
		return outXML;
	}
	
	/**
	 * <br>This API returns a Documet Object from the file that it reads from the 
	 * Remote URL. Make sure the target URL contains a XML File. Otherwise this
	 * would throw an Exception.
	 * @param fileURL java.lang.String
	 * @return org.w3c.Document
	 * @throws TFGNestingException
	 */
	public Document getDocumentFromURL(String fileURL) throws TFGNestingException{
	Document doc = null;
	try{
		doc = parse(fileURL,false,false);
		}
		catch(Exception ex){
			logger.error("WSDL Error", ex);
			throw new TFGNestingException("Parser Error: ", ex);
		}
		return doc;
	}	
	
	/*** This method ensures that the output String has only     
	 * * valid XML unicode characters as specified by the     
	 * * XML 1.0 standard. For reference, please see     
	 * * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the     
	 * * standard</a>. This method will return an empty     
	 * * String if the input is null or empty.     
	 * * @param in The String whose non-valid characters we want to remove.     
	 * * @return The in String, stripped of non-valid characters.    
	 *  */    
		public static String stripNonValidXMLCharacters(String s) {

			 StringBuilder out = new StringBuilder(); // Used to hold the output.

			 int codePoint; // Used to reference the current character.

			 //String ss = "\ud801\udc00"; // This is actualy one unicode character, represented by two code units!!!.

			 //System.out.println(ss.codePointCount(0, ss.length()));// See: 1

			 int i=0;

			 while(i<s.length()) {

			 //System.out.println("i=" + i);
			 //System.out.println("  Char : " + s.charAt(i));
			 codePoint = s.codePointAt(i); // This is the unicode code of the character.
			 //System.out.println("  CodePoint : " + codePoint);

			 if ((codePoint == 0x9) || // Consider testing larger ranges first to improve speed. 

			(codePoint == 0xA) ||

			 (codePoint == 0xD) ||

			 ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||

			 ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||

			 ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF))) {

			 out.append(Character.toChars(codePoint));

			 } 

			i+= Character.charCount(codePoint); // Increment with the number of code units(java chars) needed to represent a Unicode char. 

			}

			 return out.toString();

		} 

}

