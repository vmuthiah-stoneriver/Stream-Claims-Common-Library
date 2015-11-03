package com.client.iip.integration.batch;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SFTPRemoteTasklet extends SFTPTasklet {
	
	private Logger logger = LoggerFactory.getLogger(SFTPRemoteTasklet.class);	
	
	@javax.annotation.Resource(name = "stepExecutionListener")
	private StepExecutionListenerCtxInjecter stepExecutionListener;
	
	private ChannelSftp channel;

    private boolean refreshLocalDirectory = true;
    
    private boolean deleteRemoteFiles;

    private String fileNamePattern;
    
    private String localDirectory;
	
	
	 /**
	 * @param localDirectory the localDirectory to set
	 */
	public void setLocalDirectory(String localDirectory) {
		this.localDirectory = localDirectory;
	}

	/**
	 * @param deleteLocalFiles the deleteLocalFiles to set
	 */
	public void setRefreshLocalDirectory(boolean refreshLocalDirectory) {
		this.refreshLocalDirectory = refreshLocalDirectory;
	}

	/**
	 * @param deleteRemoteFiles the deleteRemoteFiles to set
	 */
	public void setDeleteRemoteFiles(boolean deleteRemoteFiles) {
		this.deleteRemoteFiles = deleteRemoteFiles;
	}

	/**
	 * @param fileNamePattern the fileNamePattern to set
	 */
	public void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;
	}


	@Override
	 public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		 String fileName = "";
		 try{
			 	//Do not refresh if the local Files are retained, Can be used for testing purpose.
			 	if(!refreshLocalDirectory){
			 		return RepeatStatus.FINISHED;
			 	}
			 	
				if(channel == null ){
					channel = setupSFTPChannel();
				}			 
				 //Get the list of file that match the file pattern
				 Vector<LsEntry> entries = channel.ls(fileNamePattern);
				 for (LsEntry entry : entries) {
					 fileName = entry.getFilename();
					 channel.get(entry.getFilename(), new File(localDirectory).getAbsolutePath());
					 //Delete Remote File is flag is set to true
					 if(deleteRemoteFiles){
						 channel.rm(entry.getFilename());
					 }
				 }			
				 return RepeatStatus.FINISHED;
		 }catch(Exception ex){
			 ex.printStackTrace();
			logger.error("File Transmission Failed :" + fileName, ex);
			//BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm",
			//		"File Transmission Failed : " + file==null?"":file.getName(), ex);
			throw new Exception("File Transmission Failed " + fileName, ex);				 
		 }finally{
			 if(channel != null){
				 channel.getSession().disconnect();
				 channel.exit();
			 }
		 }
	 }	

}
