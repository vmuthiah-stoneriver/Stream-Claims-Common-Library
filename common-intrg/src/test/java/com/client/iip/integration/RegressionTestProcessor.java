package com.client.iip.integration;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import org.apache.commons.io.IOUtils;

import com.client.iip.integration.core.PayloadRequestProcessor;

public class RegressionTestProcessor {
	
	HashMap hm = new HashMap();
	
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
    	
    	Properties resourceMap = loadPropFromClassPath("properties/Interface_test_client.properties");
		Enumeration keys = resourceMap.keys();

		while (keys.hasMoreElements()) {
			String name = (String) keys.nextElement();
			String value = resourceMap.getProperty(name);
			hm.put(name, value);
		}
		
    }catch(Exception ex){
    	System.out.println("Exception while reading properties file");    	
    	ex.printStackTrace();
    }
		
	}
	
	public RegressionTestProcessor(){
		
		loadPropFile();
	}
	
	
	public void launchTest(){
		
		String rootFolder = (String)hm.get("TEST_ROOT_FOLDER");
		
		System.out.println("TEST_ROOT_FOLDER: " + rootFolder);
		
		String interfaceName = null;
		String url = null;
		FileInputStream input = null;
		int threadPoolSize = hm.get("CONCURRENT_THREADS")==null?1:Integer.parseInt((String)hm.get("CONCURRENT_THREADS"));
		int thinkTime = hm.get("THINK_TIME")==null?0:Integer.parseInt((String)hm.get("THINK_TIME"));
		
		try{
			//delete log files
			File errlog = new File("Error.log");
			
			if(errlog.exists()) errlog.delete();
			
			File processlog = new File("Success.log");
			
			if(processlog.exists()) processlog.delete();
			
			File statslog = new File("Stats.log");
			
			if(statslog.exists()) statslog.delete();
			
			Iterator it = hm.keySet().iterator();
		
			while(it.hasNext()){
				String name = it.next().toString();
				interfaceName = name;
				url = hm.get(interfaceName).toString();
				String testFolder=interfaceName;
				File file = new File(rootFolder + File.separator + testFolder);
				File[] fileList = file.listFiles(new FileFilter() {
				    @Override
				    public boolean accept(File pathname) {
				        String name = pathname.getName().toLowerCase();
				        return name.endsWith(".xml") && pathname.isFile();
				    }
				});
				if (fileList == null) continue;
				//System.out.println("FileList: " + fileList.length);
				ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		        PayloadRequestProcessor reqClient = PayloadRequestProcessor.getInstance();					
				for (int i=0; i <fileList.length; i++) {
					input = new FileInputStream(fileList[i]);
					String payload = IOUtils.toString(input);
					System.out.println("Calling Interface: " + interfaceName);
					//reqClient.callService(interfaceName, payload, url, hm);
					Runnable worker = new WorkerThread(interfaceName, payload, url, hm);
		            executor.execute(worker);
					System.out.println("Completed Calling: " + interfaceName);
					input.close();
					//Time in millis
					Thread.sleep(thinkTime*1000);
				}
				
				executor.shutdown();
				
		        while (!executor.isTerminated()) {
		        }
		        //Write to the stats file

		        reqClient.logStatsFile(interfaceName);
			}
			//Send Test Results to the Mail Group.
			
			if(hm.get("SEND_MAIL").equals("Y")){
				 File infile = new File("Stats.log");

					List<String> lines = FileUtils.readLines(infile);

				 String messagebody  = " The Results of the Regression test conducted on SR Interfaces are shown below, Please refere to the Error.log attachment for "
						 				+ "investigating the failure cases.\n";
				 
				for (int i=0; i<lines.size(); i++){
					messagebody += lines.get(i) + "\t\n";
				}				 
				
				messagebody += "\n============================ AUTO GENERATED PLEASE DONOT RESPOND ===============================";
				File[] attachments = new File[]{new File("Error.log"), new File("Success.log")};
				System.out.println("Sending Email");
				sendEmail(messagebody,attachments);
				System.out.println("Email Sent");
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
       
    	String subject = "Claim Stream Interface Regression Test Results";         
    	String filename = "Error.log";           
    	Properties properties = new Properties();
    	properties.put("mail.smtp.starttls.enable", "true");
    	properties.put("mail.smtp.host", hm.get("EMAIL_HOST").toString());         
    	properties.put("mail.smtp.port", hm.get("EMAIL_PORT").toString());
    	//properties.put("mail.smtp.user", "guru.radhakrishnan");
    	//properties.put("mail.smtps.password", "Gskr0612");
    	properties.put("mail.smtp.auth", "true");
    	Authenticator auth = new Authenticator() {                  
    		@Override                 
    		protected PasswordAuthentication getPasswordAuthentication() {                     
    			return new PasswordAuthentication(hm.get("EMAIL_USER").toString(), hm.get("EMAIL_PASSWORD").toString());                 
    			}             
    		};     	
    	 
    		properties.put("mail.smtp.debug", "true"); 
    		//properties.put("mail.smtp.socketFactory.port", "587") ;
    		//properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    		//properties.put("mail.smtp.socketFactory.fallback", "false");  	
    		Session session = Session.getDefaultInstance(properties, auth);
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
    			System.out.println("Exception Ocurred while sending Email: " + e.toString());    			
    			e.printStackTrace();         
    		}     
    	} 

	
	public static void main(String[] args){
		System.out.println("Before Launching Client");
		RegressionTestProcessor regTest = new RegressionTestProcessor();
		
		regTest.launchTest();
		
		
	}
		
}