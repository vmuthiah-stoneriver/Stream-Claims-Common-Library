package com.client.iip.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.BatchRequestProcessor;

public class BatchJobLauncher {
	
	private static final Logger logger = LoggerFactory.getLogger(BatchJobLauncher.class);

	HashMap<String, String> hm = new HashMap<String, String>();
	
	public Properties loadPropFromClassPath(String propFileName) throws Exception{
		// loading properties from the classpath 
		Properties props = new Properties(); 
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(propFileName); 
		if (inputStream == null) { 
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath"); } 
		props.load(inputStream); 
		return props;		
		
	}
	
	public void loadPropFile() {
			
    try{
    	
    	Properties resourceMap = loadPropFromClassPath("properties/Batch_client.properties");
		Enumeration keys = resourceMap.keys();

		while (keys.hasMoreElements()) {
			String name = (String) keys.nextElement();
			String value = resourceMap.getProperty(name);
			hm.put(name, value);
		}
		
		//Initialize additional properties
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if(hm.get("ACCTMONTH") == null){
			hm.put("ACCTMONTH", String.valueOf(cal.get(Calendar.MONTH)+1));
		}
		if(hm.get("ACCTYEAR") == null){
			hm.put("ACCTYEAR", String.valueOf(cal.get(Calendar.YEAR)));			
		}
		if(hm.get("ACCTBASIS") == null){
			hm.put("ACCTBASIS", "cal");	
		}
		if(hm.get("BUSDATE") == null){
			hm.put("BUSDATE", "1970-01-01");	
		}
		if(hm.get("FREQUENCY") == null){
			hm.put("FREQUENCY", "dd");
		}
		
    }catch(Exception ex){
    	logger.info("Exception while reading properties file");    	
    	ex.printStackTrace();
    }
		
	}
	
	public BatchJobLauncher(){
		
		loadPropFile();
	}
	
	public void launch(String jobName, String mode, String frequency){
		
		logger.info("Launching Batch Job with JobName : " + jobName + " Mode : " + mode);
		
		if(!frequency.equals("DEFAULT")){
			hm.put("FREQUENCY", "mm");
		}
		
		FileInputStream input = null;
		
		try{
			//delete log files
			File errlog = new File("Error.log");
			
			if(errlog.exists()) errlog.delete();
			
			File processlog = new File("Success.log");
			
			if(processlog.exists()) processlog.delete();
			
			File statslog = new File("Stats.log");
			
			if(statslog.exists()) statslog.delete();
			
			Map<String, String> treeMap = new TreeMap<String, String>(hm);
			
			Iterator it = treeMap.keySet().iterator();
			boolean runFlag = false;
			while(it.hasNext()){
				String name = it.next().toString();
				if(name.startsWith("Job")){
					if(!runFlag && !jobName.equals(hm.get(name)) && mode.equals("RESTART")) continue;
					if(!jobName.equals(hm.get(name)) && mode.equals("RUN")) continue;
			        BatchRequestProcessor batchRequest = BatchRequestProcessor.getInstance();					
					String status = batchRequest.runJob((String)hm.get(name), hm);
					logger.info("Job : " + hm.get(name) + " Ended with Status : " + status);
					runFlag = true;
					batchRequest.logStatsFile((String)hm.get(name), status);
					if(status.equals("FAILED")) break;
				}else if(name.startsWith("SJob")){
					if(!mode.equals("RUN")) continue;
					if(!jobName.equals(hm.get(name)) && mode.equals("RUN")) continue;
			        BatchRequestProcessor batchRequest = BatchRequestProcessor.getInstance();					
					String status = batchRequest.runJob((String)hm.get(name), hm);
					logger.info("Job : " + hm.get(name) + " Ended with Status : " + status);
					batchRequest.logStatsFile((String)hm.get(name), status);
				}
			}
			//Send Test Results to the Mail Group.
			
			if(hm.get("SEND_MAIL").equals("Y")){
				File infile = new File("Stats.log");
				if(infile.exists()){
					List<String> lines = FileUtils.readLines(infile);
	
					 String messagebody  = " The Results of the Batch Jobs that ran as of " +  new Date()  + " are outlined below, Please refer to the Error.log attachment for "
							 				+ "investigating the failures if any.\n";
					 
					for (int i=0; i<lines.size(); i++){
						messagebody += lines.get(i) + "\t\n";
					}				 
					
					messagebody += "\n============================ AUTO GENERATED PLEASE DONOT RESPOND ===============================";
					File[] attachments = new File[]{new File("Error.log"), new File("Success.log")};
					logger.info("Sending Email");
					sendEmail(messagebody,attachments);
					logger.info("Email Sent");
				}
			}
			
		}catch (Exception e) {
			e.printStackTrace();			
		}
		finally{
			try{
				if ( input != null ){
					input.close();
				}
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
		
	    public void sendEmail(String messageBody, File[] attachments) {         
	    	String subject = "Claim Stream Batch Job Results";         
	    	String filename = "Error.log";           
	    	Properties properties = new Properties();
	    	
	    	properties.put("mail.smtp.starttls.enable", "true");
	    	properties.put("mail.smtp.host", hm.get("EMAIL_HOST").toString());         
	    	properties.put("mail.smtp.port", hm.get("EMAIL_PORT").toString());
	    	//properties.put("mail.smtp.user", "guru.radhakrishnan");
	    	//properties.put("mail.smtps.password", "Gskr0612");
	    	properties.put("mail.smtp.auth", hm.get("SMTP_AUTH").toString());
	    	properties.put("mail.smtp.ssl.trust", hm.get("EMAIL_HOST").toString());
	    	Authenticator auth = new Authenticator() {                  
	    		@Override                 
	    		protected PasswordAuthentication getPasswordAuthentication() {                     
	    			return new PasswordAuthentication(hm.get("EMAIL_USER")==null?"":hm.get("EMAIL_USER").toString(), 
	    					hm.get("EMAIL_PASSWORD")==null?"":hm.get("EMAIL_PASSWORD").toString());                 
	    			}
	    		};     	
	    	 
	    		properties.put("mail.smtp.debug", "true"); 
	    		//properties.put("mail.smtp.socketFactory.port", "587") ;
	    		//properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    		//properties.put("mail.smtp.socketFactory.fallback", "false");  	
	    		Session session = Session.getDefaultInstance(properties, hm.get("SMTP_AUTH").toString().equals("false")?null:auth);
	    		session.setDebug(true);
	    	try {
	    		//Initialize Recepient Array.
	    		String strRecepient = hm.get("RECEIPIENTS").toString();
	    		StringTokenizer st = new StringTokenizer(strRecepient, ",");
	    		//ArrayList emailList = new ArrayList();
	    		ArrayList<InternetAddress> emailList = new ArrayList<InternetAddress>(); 
	    		while(st.hasMoreTokens()) { 
	    			emailList.add(new InternetAddress(st.nextToken())); 
	   			}
	    		
	    		 InternetAddress[] addressTo = new InternetAddress[emailList.size()];
	    		 addressTo = emailList.toArray(addressTo);

	    		MimeMessage message = new MimeMessage(session);             
	    		message.setFrom(new InternetAddress(hm.get("FROM_ALIAS").toString()));             
	    		//message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));   
	    		message.setRecipients(Message.RecipientType.TO, addressTo);
	    		message.setSubject(subject);             
	    		message.setSentDate(new Date());               
	    		//Set the email message text.           
	    		MimeBodyPart messagePart = new MimeBodyPart();
	    		messagePart.setText(messageBody);          
	  			Multipart multipart = new MimeMultipart();
	   			multipart.addBodyPart(messagePart);    			
	        		// Set the email attachment file  
	    			for( int i = 0; i < attachments.length; i++ ) {
	    				if(!attachments[i].exists()) continue;
	    				MimeBodyPart attachmentPart = new MimeBodyPart();
	    				FileDataSource fileDataSource =new FileDataSource(attachments[i]) {                 
	    	    			@Override                
	    	    			public String getContentType() {                     
	    	    				return "application/octet-stream";                 
	    	    				}             
	    	    			};
	    	    		attachmentPart.setDataHandler(new DataHandler(fileDataSource));
	    	    		attachmentPart.setFileName(attachments[i].getName());
	    				multipart.addBodyPart(attachmentPart);
	    			}
	    			
	    			message.setContent(multipart);
	    			Transport transport = session.getTransport("smtp");
	    			transport.send(message);
	    			transport.close();
	    		} 
	    		catch (MessagingException e) {
	    			logger.info("Exception Ocurred while sending Email: " + e.toString());    			
	    			e.printStackTrace();         
	    		}     
	    	}		
	
	
	public static void main(String[] args) throws Exception{
		logger.info("Batch Launch Begin : ");
		BatchJobLauncher batch = new BatchJobLauncher();
		if((args.length == 2 && !args[0].equals("RESTART") && !args[0].equals("RUN"))
				|| args.length == 1 )
				throw new Exception("Invalid Argument - Valid Arguments are RESTART <jobName> or RUN <jobName>");
		else if(args.length == 3 && (!args[0].equals("RUN") || !args[2].equals("MONTHLY")))
			throw new Exception("Invalid Argument - Valid Arguments are RUN <jobName> MONTHLY - No argument defaults to DAILY");
		batch.launch(args.length < 2?"DEFAULT":args[1], args.length < 1?"DEFAULT":args[0], args.length < 3?"DEFAULT":args[2]);
		logger.info("Batch Launch End");
	}	
}
