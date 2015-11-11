package com.client.iip.integration.batch;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;


public class FileHandlerTasklet implements Tasklet {
	
	  private static final Logger logger = LoggerFactory.getLogger(FileHandlerTasklet.class);
	  
	  private String dirPath = null;
	  
	  @Resource(name = "stepExecutionListener")  
	  private StepExecutionListenerCtxInjecter stepExecutionListener;
	  
	  public void setDirPath(String _path){
		  this.dirPath = _path;
	  }
	  
		@Override
		public RepeatStatus execute(StepContribution contribution,
				ChunkContext chunkContext) throws Exception {
		try{
			int writeCount = 0;
			//Iterate through the Sucessfully processed Files and move to bak folder
			List<File> processedFiles = (List<File>) stepExecutionListener.getJobExecutionCtx().get("processedFiles");
			int fileCount = processedFiles==null?0:processedFiles.size();
			logger.info("Processed File Count :" + fileCount);
			//Set read count.
			chunkContext.getStepContext().getStepExecution().setReadCount(fileCount);			
			if(fileCount > 0){
				for(File file : processedFiles){
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
			  		//Set read/write count.
			         ++writeCount;
				  	chunkContext.getStepContext().getStepExecution().setReadCount(writeCount);
			  		chunkContext.getStepContext().getStepExecution().setWriteCount(writeCount);
				}
				//Clear the list once the files are moved.
				processedFiles.clear();
			}else if(dirPath != null){
				//Move all files to backup folder from directory path.
				File dir = new File(dirPath);
				File[] fileList = dir.listFiles(new FileFilter() {
				    @Override
				    public boolean accept(File pathname) {
				        return pathname.isFile();
				    }
				});
				for(File file : fileList){
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
			  		//Set read/write count.
			         ++writeCount;
				  	chunkContext.getStepContext().getStepExecution().setReadCount(writeCount);
			  		chunkContext.getStepContext().getStepExecution().setWriteCount(writeCount);			          
				}
				
			}
			
			return RepeatStatus.FINISHED;
			
		}catch(Exception ex){
			logger.error("File backup failed : ", ex);
			//BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm", "File backup failure : ", ex);
			throw new Exception("File backup failed : ", ex);				
		}
	}
}
