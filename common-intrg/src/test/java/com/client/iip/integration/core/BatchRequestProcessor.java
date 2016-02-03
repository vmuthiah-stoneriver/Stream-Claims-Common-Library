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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchRequestProcessor {
	
	private final Logger logger = LoggerFactory.getLogger(BatchRequestProcessor.class);
	
	public static BatchRequestProcessor batchRequestProcessor = new BatchRequestProcessor();	
	
    public static HashMap statsMap = new HashMap();
    
    public HttpClient client = null;
    
	public String runJob(String jobName, HashMap config) throws Exception{

		String jobStatus = "COMPLETED";
		long responseTimeMilliSeconds = 0;
		long startTime = new Date().getTime();
		setupHttpClient(config.get("HOST").toString().trim());
		String responseString = null;
		String strPollInterval = config.get("POLLING_FREQ").toString();
		PostMethod method = new PostMethod(config.get("HOST").toString().trim() +"/iip/services/batchJobSubmit");
		boolean runOnSystemDate = config.get("RUN_DATE").toString().equals("BUSINESS")?false:true;
		//method.setRequestHeader("Authorization", "Basic c3lzYWRtaW46dGVzdA==");
		method.addRequestHeader("TOKEN_ID", config.get("TOKEN_ID").toString());
		method.addRequestHeader("TRACKING_ID", config.get("TRACKING_ID").toString());
		method.addRequestHeader("PRINCIPLE_ID", config.get("PRINCIPLE_ID").toString());
        method.setRequestBody(generateSubmitBatchPayload(jobName, runOnSystemDate, config));
	try{
		  client.executeMethod(method);
	      responseString =  method.getResponseBodyAsString();
	      responseTimeMilliSeconds = new Date().getTime() - startTime;
	      logger.info("Job : " + jobName + " JobSubmission Response : " + responseString);
	      statsMap.put(jobName,Long.toString(responseTimeMilliSeconds));
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	jobStatus = "FAILED";
			logErrorFile(jobName, responseString==null?e.toString():responseString, responseTimeMilliSeconds);
		    return jobStatus;
	    } finally {
	      method.releaseConnection();
	    }
	
		if(responseString != null ){
			if(responseString.indexOf("xception") != -1 || responseString.indexOf("ErrorResponse") != -1 || responseString.indexOf("NullPayload") != -1) {
				jobStatus = "FAILED";
				logErrorFile(jobName, responseString, responseTimeMilliSeconds);
			    return jobStatus;
			}else{
				String strJobId = "0";
				try{
					strJobId = XMLUtility.getInstance().fetchNodeValue(responseString, "/long");
					Long jobId = Long.parseLong(strJobId);
					if(jobId.compareTo(new Long(0)) == 0) throw new Exception("Job " + jobName +" Submission Failed");
				}catch(Exception ex){
					jobStatus = "FAILED";
					logErrorFile(jobName, responseString==null?ex.toString():responseString, responseTimeMilliSeconds);
					return jobStatus;
				}
				//If successful then run Inquiry until job completes
				String batchJobStatus = "";
				while(!batchJobStatus.contains("btchprcscmp")){
				//Sleep for the Polling interval
				Thread.sleep(Integer.parseInt(strPollInterval) * 1000);	
				
				method = new PostMethod(config.get("HOST").toString() +"/iip/services/batchJobStatusInquiry");
				method.addRequestHeader("TOKEN_ID", config.get("TOKEN_ID").toString());
				method.addRequestHeader("TRACKING_ID", config.get("TRACKING_ID").toString());
				method.addRequestHeader("PRINCIPLE_ID", config.get("PRINCIPLE_ID").toString());		
			    method.setRequestBody(generateBatchStatusInquiryPayload(strJobId));
				try{
					  responseString = null;
					  client.executeMethod(method);
				      responseString =  method.getResponseBodyAsString();
				      logger.info("Job : " + jobName + " JobStatus - Response : " + responseString);
				      responseTimeMilliSeconds = new Date().getTime() - startTime;
				      statsMap.put(jobName,Long.toString(responseTimeMilliSeconds));
						if(responseString.indexOf("xception") != -1 || responseString.indexOf("ErrorResponse") != -1 || responseString.indexOf("NullPayload") != -1) {
							jobStatus = "FAILED";
						}else{
					      batchJobStatus =  XMLUtility.getInstance().fetchNodeValue(responseString, "/batchLogDTO/batchLogTypeCode");
					      String jobDescription = XMLUtility.getInstance().fetchNodeValue(responseString, "/batchLogDTO/description");
					      
					      if(!batchJobStatus.contains("btchprcscmp")) continue;
					      
					      if(jobDescription.contains("WITH ERROR")){
					    	  jobStatus = "COMPLETED WITH ERROR";
					      }else if(jobDescription.contains("COMPLETED")){
					    	  jobStatus = "COMPLETED";
					      }else{
					    	  jobStatus = "FAILED";
					      }
						}
				      
				      if(jobStatus.equals("FAILED") || jobStatus.equals("COMPLETED WITH ERROR")) throw new Exception("Job " + jobName + " Failed.");
	      
					}catch (Exception e) {
						//jobStatus = "FAILED";
						logErrorFile(jobName, responseString==null?e.toString():responseString, responseTimeMilliSeconds);
					    return jobStatus;
				    }finally{
				      method.releaseConnection();
				    }

				}
			      responseTimeMilliSeconds = new Date().getTime() - startTime;
			      statsMap.put(jobName, Long.toString(responseTimeMilliSeconds));
			      logSuccessFile(jobName, responseString, responseTimeMilliSeconds);
			}
		}
		
		return jobStatus;

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
	        client = new HttpClient();
	        client.getHostConfiguration().setHost(url.getHost(), url.getPort(), authhttps);	    
		}else{
			client = new HttpClient();
		}
		
	}
	
	public String generateSubmitBatchPayload(String jobName, boolean runOnSystemDate, HashMap config){		
		StringBuffer  payload = new StringBuffer();				
		payload.append("<clientBatchJobRequest>\n");
		payload.append("<jobName>" + jobName +"</jobName>\n");
		payload.append("<runOnSystemDate>" + runOnSystemDate + "</runOnSystemDate>\n");
		payload.append("<runType>"+  config.get("FREQUENCY").toString() +"</runType>\n");
		if(jobName.equals("glBalance")){
			
			if(!config.get("COMPANYID").toString().isEmpty()){
				payload.append("<companyID>"+  config.get("COMPANYID").toString() +"</companyID>\n");
			}
			if(!config.get("ACCTMONTH").toString().isEmpty()){
				payload.append("<acctgPeriodMonth>"+  config.get("ACCTMONTH").toString() +"</acctgPeriodMonth>\n");
			}
			if(!config.get("ACCTYEAR").toString().isEmpty()){
				payload.append("<acctgPeriodYr>"+ config.get("ACCTYEAR").toString() +"</acctgPeriodYr>\n");
			}
			if(!config.get("ACCTBASIS").toString().isEmpty()){
				payload.append("<acctgYearBasisCd>"+  config.get("ACCTBASIS").toString() +"</acctgYearBasisCd>\n");
			}
			
			payload.append("<busDate>"+  config.get("BUSDATE").toString() + " 00:00:00.0" +"</busDate>\n");	
		}
		payload.append("</clientBatchJobRequest>");
		logger.info("Batch Request Payload : " + payload.toString());
		return payload.toString();
	}
	
	public String generateBatchStatusInquiryPayload(String strJobId){
		String payload = "<long>" + strJobId + "</long>";
		return payload;
	}	
	
	public void logErrorFile(String jobName, String _message, long responseTime){
		
		RandomAccessFile raf = null;
		try{
			
			raf = new RandomAccessFile("Error.log", "rw");
			raf.skipBytes( (int)raf.length() );

			raf.writeBytes("=================== Batch Job : " + jobName + " Response Time: " + responseTime + " ms =====================\n");
			raf.writeBytes(_message);
			raf.writeBytes("\n");

	
		/*BufferedWriter out = new BufferedWriter(new FileWriter("Error.log",true)); 
		
		 out.write("===================" + interfaceName + " " + "Response Time: " + responseTime + " =====================");
		 
		 out.write("_message");
		 
		 out.newLine(); 
		 
		 out.close();*/
		}catch(Exception ex){
			logger.info("Exception while writing to file: " + ex.toString());
		}
		finally{
			try{
			if(raf != null) 
				raf.close();
			}catch(Exception ex){
				logger.info("Exception while writing to file: " + ex.toString());
			}			
			
		}
		
		
	}
	
	public void logSuccessFile(String jobName, String _message, long responseTime){
		
		RandomAccessFile raf = null;
		try{
			
			raf = new RandomAccessFile("Success.log", "rw");
			raf.skipBytes( (int)raf.length() );

			raf.writeBytes("=================== Batch Job : " + jobName + " Response Time: " + responseTime + " ms =====================\n");
			raf.writeBytes(_message);
			raf.writeBytes("\n");

	
		/*BufferedWriter out = new BufferedWriter(new FileWriter("Error.log",true)); 
		
		 out.write("===================" + interfaceName + " " + "Response Time: " + responseTime + " =====================");
		 
		 out.write("_message");
		 
		 out.newLine(); 
		 
		 out.close();*/
		}catch(Exception ex){
			logger.info("Exception while writing to file: " + ex.toString());
		}
		finally{
			try{
			if(raf != null) 
				raf.close();
			}catch(Exception ex){
				logger.info("Exception while writing to file: " + ex.toString());
			}			
			
		}
		
		
	}
	
	public void logStatsFile(String jobName, String jobStatus){
		
		File file = null;
		try{
			
			file = new File("Stats.log");
			if( ! file.exists()){
				file.createNewFile();
			}
			String outString = "";
			boolean writeFirstTime = true;
	
			List<String> lines = FileUtils.readLines(file);
			for (int i=0; i<lines.size(); i++){
				String lineData = lines.get(i);
				if(lineData.indexOf(jobName) >= 0){
					outString += jobName + " Completed with status code: " + jobStatus +  
								" Response Time: " + Integer.parseInt((String)statsMap.get(jobName)) + " ms\n";
					writeFirstTime = false;
				}else{
					outString += lineData + "\n" ;
				}
				
			}
			
			if(writeFirstTime){
				outString += jobName + " Completed with status code: " + jobStatus +  
						" Response Time: " + Integer.parseInt((String)statsMap.get(jobName)) + " ms\n";
			}
			FileUtils.writeStringToFile(file, outString);
			
		}catch(Exception ex){
			logger.info("Exception while writing to file: " + ex.toString());
		}
	
		
	}
	
	
	public static BatchRequestProcessor getInstance(){
		return batchRequestProcessor;
	}


}
