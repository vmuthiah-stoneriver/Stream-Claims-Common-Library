package com.client.iip.integration.core.webservice;
/*
 * !!!! WARNING - This class is not thread safe - Prototype this !!!!!
 */
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.cache.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.client.iip.integration.core.util.XMLUtility;
import com.fiserv.isd.iip.core.cache.jbosscache.JBossCacheAccessor;
import com.fiserv.isd.iip.core.cache.jbosscache.JBossCacheManager;
import com.freedomgroup.common.exception.TFGNestingException;

public class SOAPMessageBuilder {
	
	private Logger logger = LoggerFactory.getLogger(SOAPMessageBuilder.class);
	private static ResourceBundle resBundle = ResourceBundle.getBundle("properties/webservice-operation-bindings");
	
	private static final String DOM_CACHE = "client.iip.integration.DOMCache";
	
	private Document document = null;
	private String SOAPOperation = null;
	
	public SOAPMessageBuilder(String wsdlURL, String operationName) throws Exception{
		//Check if the wsdl is in cache, if not add to the cache
		Document doc = null;
		try{
			JBossCacheManager cacheMgr = JBossCacheAccessor.getJBossCache();
			Node <Object, Object> cacheNode = cacheMgr.getAppDataCache();
			Map<String, Document> domMap = (Map<String, Document>)cacheNode.get(DOM_CACHE);
			if(domMap == null) {
				domMap = new ConcurrentHashMap<String, Document>();
				cacheNode.put(DOM_CACHE, domMap);
			}
			doc = domMap.get(wsdlURL);
			if(doc == null) {
				doc = XMLUtility.getInstance().getDocumentFromURL(wsdlURL);     
				domMap.put(wsdlURL, doc);
			}
			this.document = doc;
			this.SOAPOperation = operationName;
		}catch(TFGNestingException ex){
			logger.error("Exception occurred while processing the wsdl :" , ex);
			throw ex;
		}

	}
	
	public String buildGenericSOAPMessage(String sendMsg) throws Exception{
		String soapEnvelope = "<?xml version='1.0' encoding='UTF-8'?>"
				+ "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' "
				+ "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
					+ "xmlns:xsd='http://www.w3.org/2001/XMLSchema'> ";
		String opName = getOperationName();
		String soapURI = "<tns:" + opName + " xmlns:tns='" + getTargetURI() + "'>";
		
		String	soapBody = null;
		String param = getParameterName();
		if (param == null){
			soapBody = sendMsg;
		}else{
			soapBody = "<" + param + ">"
							+ "<![CDATA["
							+sendMsg
							+ "]]>"
							+ "</" + param + ">";
		}
				
		String soapMessage = soapEnvelope +  "<soap:Body>" + soapURI
						+ soapBody + "</tns:" + opName +">"
						+ "</soap:Body>" + "</soap:Envelope>";		
		
		return soapMessage;
	}
	
	public String buildDefaultSOAPMessage(String sendMsg) throws Exception{
		String soapEnvelope = "<?xml version='1.0' encoding='UTF-8'?>"
				+ "<soap:Envelope xmlns:soap='http://schemas.xmlsoap.org/soap/envelope/' "
				+ "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
					+ "xmlns:xsd='http://www.w3.org/2001/XMLSchema'> ";
		String opName = getOperationName();
		String soapURI = "<tns:" + opName + " xmlns:tns='" + getTargetURI() + "'>";
		
		String soapMessage = soapEnvelope +  "<soap:Body>" + soapURI
						+ sendMsg + "</tns:" + opName +">" + "</soap:Body>" + "</soap:Envelope>";		
		
		return soapMessage;		
	}
	
	public String getRouterURL() throws Exception{
		String routerURL = XMLUtility.fetchNodeValue("/definitions/service/port/address/@location", document);
		logger.info("Router URL : " + routerURL);
		return routerURL;
	}
	
	public String getTargetURI() throws Exception{
		String targetURI = XMLUtility.fetchNodeValue("/definitions/@targetNamespace", document);
		logger.info("targetURI: " + targetURI);
		return targetURI;
	}	
	
	public String getSOAPAction() throws Exception{
		String SOAPAction = XMLUtility.fetchNodeValue("/definitions/binding/operation[@name= '" + this.SOAPOperation + "']/operation/@soapAction", document);
		if(SOAPAction == null) {
				SOAPAction = getTargetURI();
		}
		logger.info("SOAPAction: " + SOAPAction);
		return SOAPAction;		
	}
	
	public String getOperationName() throws Exception{

		if(SOAPOperation == null){//Fetch Default Operation if null
			return XMLUtility.fetchNodeValue("/definitions/binding/operation/@name", document);
			//defaultArg =  XMLUtility.fetchNodeValue("/definitions/types/schema/complexType[@name='"+ 
			//										defaultFunctionName +"']/sequence/element/@name", document);
		}else{
			return SOAPOperation;
		}
 
	}
	
	public String getParameterName(){
		String param = null;
		try{
			if(SOAPOperation == null){//Fetch Default Operation if null
					String defaultFunctionName =  XMLUtility.fetchNodeValue("/definitions/binding/operation/@name", document);
					param = XMLUtility.fetchNodeValue("/definitions/types/schema/complexType[@name='"+ 
					defaultFunctionName +"']/sequence/element/@name", document);
					return param;
				}else{
					//Lookup the Webservice operations bindings file
					param =	(String)resBundle.getString(SOAPOperation);
					return param;			
				}
			}catch(Exception ex){
				//return default param as NONE so the message is embedded as is within the function tags
				return param;
			}

		}

}
