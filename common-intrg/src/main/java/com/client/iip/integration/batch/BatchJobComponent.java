package com.client.iip.integration.batch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;

import com.client.iip.integration.core.util.CloneUtil;
import com.client.iip.integration.sa.ClientConfigService;
import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.entconfig.batch.BatchLogConstants;
import com.stoneriver.iip.entconfig.batch.BatchLogDTO;
import com.stoneriver.iip.entconfig.batch.launcher.BatchJobLauncher;
import com.stoneriver.iip.entconfig.date.BusinessDateType;
import com.stoneriver.iip.entconfig.date.DateConstants;
import com.stoneriver.iip.entconfig.date.DateService;

/**
 * Claim Import Component is used for converting the ClaimImportCompositeDTO to ClaimInitiateDTO and calling the API for Claim Import.
 * 
 * @author Saurabh.Bhatnagar
 *
 */
@Pojo(id = "client.component.batchJob")
public class BatchJobComponent {
	
	private final Logger logger = LoggerFactory.getLogger(BatchJobComponent.class);
	private JobExplorer jobExplorer = null;
		
	/**
	 * @return the jobExplorer
	 */
	public JobExplorer getJobExplorer() {
		return jobExplorer;
	}

	/**
	 * @param jobExplorer the jobExplorer to set
	 */
	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}

/**
 * This method will run the Batch job on request provided by client. 
 * @param request job name
 * @return Long Job id
 */
	public Long callJob(ClientBatchJobRequest request){
		
		Long jobId = null;
		try{
			JobExecution exec = null;
			JobParametersBuilder builder = new JobParametersBuilder();
			if (request.isRunOnSystemDate()) {
				builder.addDate("runDate", DateUtility.getSystemDateTime());
				builder.addDate("lastRunDate", DateUtility.subtractDays(
					DateUtility.getSystemDateTime(), 1));
			} else {
				builder.addDate("runDate", 
	            MuleServiceFactory.getService(DateService.class)
		            .getBusinessDate(DateConstants.BUSN_DATE_CMPY_ID, 
				     BusinessDateType.BUSINESS));
				builder.addDate("lastRunDate", DateUtility.subtractDays(
					MuleServiceFactory.getService(DateService.class)
		            .getBusinessDate(DateConstants.BUSN_DATE_CMPY_ID, 
				     BusinessDateType.BUSINESS), 1));
			}
			
			//Add additional job parameters used by glBalance Job
			if(request.getRunType() != null){
				builder.addString("runType", request.getRunType());
			}
			
			if(request.getAcctgPeriodMonth() != null){
				builder.addString("acctgPeriodMonth", request.getAcctgPeriodMonth());
			}
			
			if(request.getAcctgPeriodYr() != null){
				builder.addString("acctgPeriodYr", request.getAcctgPeriodYr());
			}
			
			if(request.getAcctgYearBasisCd() != null){
				builder.addString("acctgYearBasisCd", request.getAcctgYearBasisCd());
			}
			
			if(request.getBusDate() != null){
				Calendar cal = Calendar.getInstance();
				cal.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("1970-01-01"));
				cal.setTimeZone(TimeZone.getTimeZone("UTC"));
				if(request.getBusDate().compareTo(cal.getTime()) == 0){
					logger.info("Default Business Date : " + request.getBusDate());
					builder.addDate("busDate",MuleServiceFactory.getService(DateService.class)
			            .getBusinessDate(DateConstants.BUSN_DATE_CMPY_ID, 
							     BusinessDateType.BUSINESS));
				}else{
					logger.info("Business Date : " + request.getBusDate());
					builder.addDate("busDate", request.getBusDate());
				}
			}
			
			if(request.getCompanyID() != null){
				builder.addString("company", request.getCompanyID());
			}
			
			builder.addDate("currentDate", DateUtility.getSystemDateTime());
			exec =	BatchJobLauncher.launchJob(request.getJobName(),  builder);
			ExitStatus status = exec.getExitStatus();
			//It is really the Job Execution id that we need as the Job Id.
			jobId = exec.getId();
			logger.info("Returning the job id for call job: {}", jobId);
			logger.info("Job Completed with Status: {}", status);
			logger.info("Job Completed with Exit Code: {}", exec.getStatus());
			logger.info("---------------------------------Process Ended-----------------------------------------");
			}catch(Exception ex){
				logger.error("Exception occured while executing Batch", ex);
				jobId = Long.valueOf(0);
				logger.info("Returning the job id as 0 in case of exception in call job.");				
				return jobId;
			}
		
			return jobId;
	}
	
	// Changes as per new requirement by Gaurav R. This API will accept a batch Id and use it to fetch batch status
	public BatchLogDTO retrieveJobStatus(Long jobId){
		BatchLogDTO batchLog = null;
        String statusCode;
        boolean batchInProcess = true;
        try{
			JobExecution exec = getJobExplorer().getJobExecution(jobId);
	        if(exec!=null){
	        	statusCode = exec.getStatus().toString();	
	        }else {
	        	statusCode = "";
	        }
	        logger.info("Job Completed with Exit Code: {}", statusCode);
	        //Get Batch Log details
	        Collection<BatchLogDTO> btchlogs = MuleServiceFactory.getService(ClientConfigService.class).retrieveBatchLog(jobId);
	         
	        for(BatchLogDTO log: btchlogs){
	        	//Look for Completed log.
	        	if(log.getBatchLogTypeCode().equals(BatchLogConstants.BATCH_PROCESS_COMPLETED)){
	        		batchLog = log;
	        		batchInProcess = false;
	        		//Check if there are any errors in the process and set description accordingly.
	        		for(BatchLogDTO _log: btchlogs){
	        			if(_log.getBatchLogTypeCode().equals(BatchLogConstants.BATCH_PROCESS_ERROR)){
	        				if(!batchLog.getDescription().contains("COMPLETED WITH ERROR")){
	        					batchLog.setDescription(batchLog.getDescription().replaceAll("COMPLETED", "COMPLETED WITH ERROR"));
	        				}
	        			}
	        		}
	        		break;
	        	}else{
	        		batchLog = log;
	        	}
	        }
	        
	        batchLog = (BatchLogDTO)CloneUtil.deepClone(batchLog);
	        
	        //Return if the batch is still running
	        if(batchInProcess) return batchLog;
	        
	        //Clear the detail and Assign all detail info for that batch job
	        batchLog.getBatchLogDetail().clear();
	        Iterator<BatchLogDTO> itBatchLog = btchlogs.iterator();	       
	        while(itBatchLog.hasNext()){
	        	BatchLogDTO log = itBatchLog.next();
	        	if(!log.getBatchLogDetail().isEmpty()){
	        		batchLog.getBatchLogDetail().addAll(log.getBatchLogDetail());
	        	}
	        }
	        return batchLog;
        }catch(Exception ex){
			logger.error("Exception occured while executing Batch", ex);			
			throw new IIPCoreSystemException("Exception occured while executing Batch", ex);
        }
	}

}
