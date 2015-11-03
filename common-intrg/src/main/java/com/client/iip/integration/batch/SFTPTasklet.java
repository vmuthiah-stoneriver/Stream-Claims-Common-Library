package com.client.iip.integration.batch;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.ResourcesItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
 
@Component
public class SFTPTasklet extends ResourcesItemReader implements Tasklet {
	
	private static final Logger logger = LoggerFactory.getLogger(SFTPTasklet.class);
	
	@javax.annotation.Resource(name = "stepExecutionListener")
	private StepExecutionListenerCtxInjecter stepExecutionListener;
	
	private ChannelSftp channel;
	
	private String user;
	
	private String password;
	
	private String host;
	
	private int port;	

	private String remoteDirectory;
	
    private String privateKeyPath;
    
    private String privateKeyPassphrase;	


	/**
	 * @param privateKeyPath the privateKeyPath to set
	 */
	public void setPrivateKeyPath(String _privateKeyPath) {
		this.privateKeyPath = _privateKeyPath.equals("/default")?"":_privateKeyPath;
	}

	/**
	 * @param privateKeyPassphrase the privateKeyPassphrase to set
	 */
	public void setPrivateKeyPassphrase(String _privateKeyPassphrase) {
		this.privateKeyPassphrase = _privateKeyPassphrase;
	}

	public void setLocalDirectory(Resource[] resources) {
		super.setResources(resources);
	}
	
	public void setRemoteDirectory(String _remoteDir) {
		remoteDirectory = _remoteDir;
	}
	
	public void setHost(String _host) {
		host = _host;	
	}
	
	public void setPort(String _port) {
		port = Integer.parseInt(_port);		
	}
	
	public void setUser(String _user) {
		user = _user;
	}
	
	public void setPassword(String _password) {
		password = _password;
	}	
 
	
	 @Override
	 public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		 File file = null;
		 try{
			Resource res = super.read();
			while(res != null) {
				file = res.getFile();
				if (file.exists()) {
					/*Message message = MessageBuilder.withPayload(file).build();
				    sftpChannel.send(message);*/
					if(channel == null ){
						channel = setupSFTPChannel();
					}		            

		            FileInputStream fileInput = new FileInputStream(file);
		            
		            channel.put(fileInput, file.getName());

		            fileInput.close();
				    //If sucessful then move the file to the bak folder
				    backupFile(file);
				} else {
				  logger.error("File does not exist.");
				}
				res = super.read();
			}
			
			return RepeatStatus.FINISHED;
		 }catch(Exception ex){
			logger.error("File Transmission Failed :" + file==null?"":file.getName(), ex);
			//BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm",
			//		"File Transmission Failed : " + file==null?"":file.getName(), ex);
			throw new Exception("File Transmission Failed " + file==null?"":file.getName(), ex);				 
		 }finally{
			 if(channel != null){
				 channel.getSession().disconnect();
				 channel.exit();
			 }
		 }
	 }
	 
	 public ChannelSftp setupSFTPChannel() throws Exception{
		 
	        JSch jsch = new JSch();
	        Session session;

	        logger.debug("Setting up session: " + user + ":" + host + ":" + port);

	        if (StringUtils.isNotEmpty(privateKeyPath) && StringUtils.isNotEmpty(privateKeyPassphrase)) {
	            logger.debug("Adding Security Identity: " + privateKeyPath + ":" + privateKeyPassphrase);
	            jsch.addIdentity(privateKeyPath, privateKeyPassphrase);
	            session = jsch.getSession(user, host, port);
	        } else {
	            session = jsch.getSession(user, host, port);
	            session.setPassword(password);
	        }
            
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channel = (ChannelSftp)session.openChannel("sftp");            
            channel.connect();            
            channel.cd(remoteDirectory);
            return channel;
	 }
 
	 public void backupFile(File file) throws Exception{
		  File oldFile = new File(file.getAbsolutePath());
		  File destDir = new File(file.getParent() + File.separator + "bak");
		  //Create Directory if does not exists
			if(!destDir.isDirectory()){
				destDir.mkdir();
			}	          
			File newFile = new File(destDir + File.separator + FilenameUtils.getBaseName(oldFile.getName()) + ".processed");
	        //Check if newFile exists in destination. If present delete then rename the file before moving to avoid IOException..
	        if(newFile.exists()){
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH-mm-ss.SSS");
				String formattedDate = dateFormat.format(new Date());			        	
				newFile = new File(destDir + File.separator + FilenameUtils.getBaseName(oldFile.getName()) + "_" + formattedDate + ".processed");
	        }		  
	        FileUtils.moveFile(oldFile, newFile);		 
	 }


}
