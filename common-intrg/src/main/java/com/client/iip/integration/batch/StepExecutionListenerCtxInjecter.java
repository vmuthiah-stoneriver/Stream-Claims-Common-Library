package com.client.iip.integration.batch;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;

public class StepExecutionListenerCtxInjecter {
	
	/**
     * logger for this class.
     */
	private static final Logger logger = LoggerFactory.getLogger(StepExecutionListenerCtxInjecter.class);	
	
	private ExecutionContext stepExecutionCtx;           
	private ExecutionContext jobExecutionCtx; 
	private StepExecution stepExecution;
	
	@BeforeStep     
	public void beforeStep(StepExecution _stepExecution)     
	{
		logger.info("Before Step Called");
		stepExecution = _stepExecution;
		stepExecutionCtx = _stepExecution.getExecutionContext();         
		jobExecutionCtx = _stepExecution.getJobExecution().getExecutionContext();
		List<File> processedFiles = (List<File>) jobExecutionCtx.get("processedFiles");
		if(processedFiles != null){
			processedFiles.clear();
		}
	}
	
	public StepExecution getStepExecution()     
	{         
		return stepExecution;     
	} 	
	
	public ExecutionContext getStepExecutionCtx()     
	{        
		return stepExecutionCtx;
	}       
	
	public ExecutionContext getJobExecutionCtx()     
	{         
		return jobExecutionCtx;     
	} 
}
