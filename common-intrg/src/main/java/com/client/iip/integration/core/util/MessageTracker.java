package com.client.iip.integration.core.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;


public class MessageTracker {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MessageTracker.class);	
	
	private static final String TRACKING_FILE_PATH_NAME = "security.tracking.file.path.name";
	private static final Logger FileWriter = Logger.getLogger("Tracker");
	private static boolean Enabled = false;
	static{
		try{
			String fileName = System.getProperty(TRACKING_FILE_PATH_NAME);
			if(fileName != null){
				FileWriter.setAdditivity(false);
				FileWriter.setLevel(Level.INFO);
				PatternLayout layout = new PatternLayout("%m%n");			
				RollingFileAppender file = new RollingFileAppender(layout, fileName, true);
				file.setMaxFileSize("20MB");
				file.setMaxBackupIndex(10);
				FileWriter.addAppender(file);
				Enabled = true;
			}
		}catch(Exception ex){
			logger.error("Error While loading Tracker File", ex);
		}
	}
	
	public MessageTracker(){
		
	}
	
	
	public static boolean isEnabled(){
		return Enabled;
	}

	
	public static void write(String message){
		FileWriter.info(message);
	}

}
