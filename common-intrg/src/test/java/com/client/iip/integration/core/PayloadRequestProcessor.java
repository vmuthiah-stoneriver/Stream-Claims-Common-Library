package com.client.iip.integration.core;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.contrib.ssl.AuthSSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.io.FileUtils;



public class PayloadRequestProcessor {
	
	public int successCount = 0;
	
	public int errorCount = 0;
	
	public int totalCount = 0;
	
	public static PayloadRequestProcessor payloadProcessor = new PayloadRequestProcessor();
	
	public long totalReponseTime = 0;
	
    public static HashMap statsMap = new HashMap();
    
    String mapKeySuccess;
    
    String mapKeyFailure;
    
    String mapKeyTotal;
    
    String mapKeyResponseTime;
    
    String mapKeyName;
    
    public HttpClient client = null;
	
	public void callService(String interfaceName, String payload, String url, HashMap config) throws Exception{
		String xsdFileName = config.get(interfaceName+"_XSD")==null?null:config.get(interfaceName+"_XSD").toString();
		long responseTimeMilliSeconds = 0;
		setupHttpClient(config.get("HOST").toString().trim());
		PostMethod method = null;
		String responseString = null;
		method = new PostMethod(config.get("HOST").toString().trim() + url);
		//method.setRequestHeader("Authorization", "Basic c3lzYWRtaW46dGVzdA==");
		method.setRequestHeader("TOKEN_ID", config.get("TOKEN_ID").toString());
		method.setRequestHeader("TRACKING_ID", config.get("TRACKING_ID").toString());
		method.setRequestHeader("PRINCIPLE_ID", config.get("PRINCIPLE_ID").toString());		
		method.setRequestBody(payload);
		mapKeySuccess = interfaceName + "_SUCCESS";
		mapKeyFailure = interfaceName + "_FAILURE";
		mapKeyTotal = interfaceName + "_TOTAL";
		mapKeyResponseTime = interfaceName + "_RESPONSETIME";
	
	try{
		  long startTime = new Date().getTime();
	      client.executeMethod(method);
	      responseString = method.getResponseBodyAsString();
	     //If the root element is a list tag then modify to rootlist since XSD Validator fails to validate the schema
	      if(responseString.startsWith("<list>")){
	    	  responseString = responseString.replaceAll("<list>", "<rootlist>");
	    	  responseString = responseString.replaceAll("</list>", "</rootlist>");
	      }
	      responseTimeMilliSeconds = new Date().getTime() - startTime;
	      totalReponseTime = statsMap.get(mapKeyResponseTime) == null?responseTimeMilliSeconds:Long.parseLong((String)statsMap.get(mapKeyResponseTime)) + responseTimeMilliSeconds;	      
	      statsMap.put(mapKeyResponseTime,Long.toString(totalReponseTime));
	      
	    } catch (Exception e) {
	    	System.err.println(e);
			errorCount = statsMap.get(mapKeyFailure) == null?1:Integer.parseInt((String)statsMap.get(mapKeyFailure)) + 1;
			statsMap.put(mapKeyFailure,Integer.toString(errorCount));
			logErrorFile(interfaceName, responseString.replaceAll("rootlist", "list"), responseTimeMilliSeconds);
	    } finally {
	      method.releaseConnection();
	    }
	
		if(responseString != null ){
			if(responseString.indexOf("xception") != -1 || responseString.indexOf("ErrorResponse") != -1 || responseString.indexOf("NullPayload") != -1) {
				errorCount = statsMap.get(mapKeyFailure) == null?1:Integer.parseInt((String)statsMap.get(mapKeyFailure)) + 1;
				statsMap.put(mapKeyFailure,Integer.toString(errorCount));
				validateResponse(responseString, "xsd/IIPErrorResponse.xsd", interfaceName, responseTimeMilliSeconds);
				logErrorFile(interfaceName, responseString.replaceAll("rootlist", "list"), responseTimeMilliSeconds);
			}else{
				//Validate with Response XSD
				boolean valid = validateResponse(responseString, xsdFileName, interfaceName, responseTimeMilliSeconds);
				if(valid){
					if(xsdFileName != null)
					System.out.println("Response xsd validation passed for: " + interfaceName);
					successCount = statsMap.get(mapKeySuccess) == null?1:Integer.parseInt((String)statsMap.get(mapKeySuccess)) + 1;
					statsMap.put(mapKeySuccess,Integer.toString(successCount));				
					logSucessFile(interfaceName, responseString.replaceAll("rootlist", "list"), responseTimeMilliSeconds);
				}
			}
		}
		
		totalCount = statsMap.get(mapKeyTotal) == null?1:Integer.parseInt((String)statsMap.get(mapKeyTotal)) + 1;
		statsMap.put(mapKeyTotal,Integer.toString(totalCount));

		//logStatsFile(interfaceName);

	}
	
	public boolean validateResponse(String payload, String xsdFileName, String interfaceName, long responseTimeMilliSeconds){
		
	try{
			XSDValidator xsdval = new XSDValidator();
			if(xsdFileName != null){
				System.out.println("XSD Validation File: " + xsdFileName);
				xsdval.validateSchema(payload, xsdFileName);
			}
			return true;
		}catch(Exception ex){
			errorCount = statsMap.get(mapKeyFailure) == null?1:Integer.parseInt((String)statsMap.get(mapKeyFailure)) + 1;
			if(!xsdFileName.equals("xsd/IIPErrorResponse.xsd")){
				statsMap.put(mapKeyFailure,Integer.toString(errorCount));
			}
			System.out.println("Response xsd validation failed for: " + interfaceName);
			logErrorFile(interfaceName, xsdFileName + " Response XSD Validation failed: " + ex.toString() + " for response: " + payload.replaceAll("rootlist", "list"), responseTimeMilliSeconds);
			return false;
		}
		
	}
	
	public void setupHttpClient(String hostURL) throws Exception{
		
		if(client != null ) return;
		
		if(hostURL.contains("https://")){
			
			URL url = new URL(hostURL);
			
			ProtocolSocketFactory factory = null;
			
			URL keyFileURL = this.getClass().getClassLoader().getResource("keystore.jks"); 
			
			factory = new AuthSSLProtocolSocketFactory(keyFileURL, "stream",
					keyFileURL, "stream");
			
	        Protocol authhttps = new Protocol("https", factory,url.getPort());
	        Protocol.registerProtocol("https", authhttps);
	        HttpClient client = new HttpClient();
	        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), authhttps);	    
		}else{
			client = new HttpClient();
		}
		
	}	
	
	public void logErrorFile(String interfaceName, String _message, long responseTime){
		
		RandomAccessFile raf = null;
		try{
			
			raf = new RandomAccessFile("Error.log", "rw");
			raf.skipBytes( (int)raf.length() );

			raf.writeBytes("===================" + interfaceName + " Response Time: " + responseTime + " ms =====================\n");
			raf.writeBytes(_message);
			raf.writeBytes("\n");

	
		/*BufferedWriter out = new BufferedWriter(new FileWriter("Error.log",true)); 
		
		 out.write("===================" + interfaceName + " " + "Response Time: " + responseTime + " =====================");
		 
		 out.write("_message");
		 
		 out.newLine(); 
		 
		 out.close();*/
		}catch(Exception ex){
			System.out.println("Exception while writing to file: " + ex.toString());
		}
		finally{
			try{
			if(raf != null) 
				raf.close();
			}catch(Exception ex){
				System.out.println("Exception while writing to file: " + ex.toString());
			}			
			
		}
		
		
	}
	
	public void logSucessFile(String interfaceName, String _message, long responseTime){
		
		RandomAccessFile raf = null;
		try{
			
			raf = new RandomAccessFile("Success.log", "rw");
			raf.skipBytes( (int)raf.length() );

			raf.writeBytes("===================" + interfaceName + " Response Time: " + responseTime + " ms =====================\n");
			raf.writeBytes(_message);
			raf.writeBytes("\n");

	
		/*BufferedWriter out = new BufferedWriter(new FileWriter("Error.log",true)); 
		
		 out.write("===================" + interfaceName + " " + "Response Time: " + responseTime + " =====================");
		 
		 out.write("_message");
		 
		 out.newLine(); 
		 
		 out.close();*/
		}catch(Exception ex){
			System.out.println("Exception while writing to file: " + ex.toString());
		}
		finally{
			try{
			if(raf != null) 
				raf.close();
			}catch(Exception ex){
				System.out.println("Exception while writing to file: " + ex.toString());
			}			
			
		}
		
		
	}
	

	public void logStatsFile(String interfaceName){
		
		File file = null;
		try{
			
			file = new File("Stats.log");
			if( ! file.exists()){
				file.createNewFile();
			}
			String outString = "";
			boolean writeFirstTime = true;
			String successCnt = statsMap.get(mapKeySuccess)==null?"0":(String)statsMap.get(mapKeySuccess);
			String failureCnt = statsMap.get(mapKeyFailure)==null?"0":(String)statsMap.get(mapKeyFailure);			
			List<String> lines = FileUtils.readLines(file);
			for (int i=0; i<lines.size(); i++){
				String lineData = lines.get(i);
				if(lineData.indexOf(interfaceName) >= 0){
					outString += interfaceName + " " + " Total Requests: " + statsMap.get(mapKeyTotal) + " Failed: " + failureCnt + " Successful: " + successCnt + 
								" Average Response Time: " + Integer.parseInt((String)statsMap.get(mapKeyResponseTime)) / Integer.parseInt((String)statsMap.get(mapKeyTotal)) + " ms\n";
					writeFirstTime = false;
				}else{
					outString += lineData + "\n" ;
				}
				
			}
			if(writeFirstTime){
				outString += interfaceName + " " + " Total Requests: " + statsMap.get(mapKeyTotal) + " Failed: " + failureCnt + " Successful: " + successCnt + 
						" Average Response Time: " + Integer.parseInt((String)statsMap.get(mapKeyResponseTime)) / Integer.parseInt((String)statsMap.get(mapKeyTotal)) + " ms\n";
			}
			FileUtils.writeStringToFile(file, outString);
			
		}catch(Exception ex){
			System.out.println("Exception while writing to file: " + ex.toString());
		}
	
		
	}
	
	
	public static PayloadRequestProcessor getInstance(){
		return payloadProcessor;
	}
	

}