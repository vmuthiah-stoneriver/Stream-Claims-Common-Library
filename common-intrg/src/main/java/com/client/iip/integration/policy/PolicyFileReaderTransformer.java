package com.client.iip.integration.policy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.transformer.AbstractMessageTransformer;

import com.client.iip.integration.core.util.XMLUtility;

public class PolicyFileReaderTransformer extends AbstractMessageTransformer {

	private static final Logger logger = Logger.getLogger(PolicyFileReaderTransformer.class);
	
	private static ResourceBundle policyMap = ResourceBundle.getBundle("properties/StreamPolicyInterface");
	
	 public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException
	    {
		 String payload = null;
		 String fileName = null;
	    	try{
	    		String methodName = message.getProperty("function", PropertyScope.OUTBOUND);
	    		payload = message.getPayloadAsString();
	    		
	    		if(methodName.equals("search")){
	    			fileName = XMLUtility.getInstance().fetchNodeValue(payload, "/claimsPolicySearchCriteriaDTO/policyNumber");
	    			payload = searchPolicy(fileName);
	    		}else if(methodName.equals("retrieveDetails")){
	    			fileName = XMLUtility.getInstance().fetchNodeValue(payload, "/policyImportRequestDTO/policyNumber");
	    			payload = retrievePolicyDetails(fileName);
	    		}else if(methodName.equals("importPolicy")){
	    			fileName = XMLUtility.getInstance().fetchNodeValue(payload, "/policyImportRequestDTO/policyNumber");
	    			payload = importPolicy(fileName);
	    		}else if(methodName.equals("reImportPolicy")){
	    			fileName = XMLUtility.getInstance().fetchNodeValue(payload, "/reImportRequestDTO/policyNumber");
	    			payload = reImportPolicy(fileName);
	    		}else if(methodName.equals("listPolicyUnits")){
	    			fileName = XMLUtility.getInstance().fetchNodeValue(payload, "/listUnitsCriteriaDTO/policyNumber");
	    			payload = listPolicyUnits(fileName);
	    		}
	    		logger.info("Response Payload: " + payload);
	    		message.setPayload(payload);
	    	}catch(Exception ex){
	    		ex.printStackTrace();
	    		payload = "<iipCoreSystemException><detailMessage>" + ex.toString().replaceAll("<","&lt;").replaceAll(">", "&gt;") + "</detailMessage></iipCoreSystemException>";
	    		message.setPayload(payload);
	    		message.setExceptionPayload(null);  
	    		return message;
	    	}

	    	return message;
	    }
	 
	 private String searchPolicy(String fileName) throws Exception{
			String response = null;
			File file = null;
		    FileInputStream input = null;
		    StringBuffer sb = new StringBuffer();
			String filePath = System.getProperty("searchPolicyFolder")==null?policyMap.getString("searchPolicyFolder")
								:System.getProperty("searchPolicyFolder");
			file = new File(filePath + File.separator);
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			sb.append("<list>\n");
			String policyNumber = fileName;
			policyNumber = policyNumber.substring(0, policyNumber.indexOf("*") < 0 ?policyNumber.length(): policyNumber.indexOf("*"));
			final String polNum = policyNumber;
			logger.info("PolicyNumber Requested: " + polNum);
			File[] fileList = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name)
	    		{
	    		    return (name.startsWith(polNum));
	    		}
	    	      });
					
			for (int i=0; i < fileList.length; i++) {
				input = new FileInputStream(fileList[i]);
				sb.append(IOUtils.toString(input));
				input.close();
			}
			sb.append("</list>");
			response = sb.toString();
			return response;
 		}
	 
	 private String retrievePolicyDetails(String fileName) throws Exception{
			String filePath = System.getProperty("retrieveDetailsFolder")==null?policyMap.getString("retrieveDetailsFolder")
					:System.getProperty("retrieveDetailsFolder");
			String strXML = FileUtils.readFileToString(new File(filePath + File.separator + fileName + ".xml"));
			return strXML;
	 }
	 
	 
	 private String importPolicy(String fileName) throws Exception{
			String filePath = System.getProperty("importPolicyFolder")==null?policyMap.getString("importPolicyFolder")
					:System.getProperty("importPolicyFolder");
			String strXML = FileUtils.readFileToString(new File(filePath + File.separator + fileName + ".xml"));
			return strXML;
	 }
	 
	 private String reImportPolicy(String fileName) throws Exception{
			String filePath = System.getProperty("refreshUnitsFolder")==null?policyMap.getString("refreshUnitsFolder")
					:System.getProperty("refreshUnitsFolder");
			String strXML = FileUtils.readFileToString(new File(filePath + File.separator + fileName + ".xml"));
			return strXML;
	 }	 
	 
	 
	 private String listPolicyUnits(String fileName) throws Exception{
			String filePath = System.getProperty("listUnitsFolder")==null?policyMap.getString("listUnitsFolder")
					:System.getProperty("listUnitsFolder");
			String strXML = FileUtils.readFileToString(new File(filePath + File.separator + fileName + ".xml"));
			return strXML;
	 }
	 
}
