package com.client.iip.integration.batch;

import org.springframework.batch.core.StepExecution;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.fiserv.isd.iip.core.date.DateUtility;
import com.fiserv.isd.iip.core.security.IIPUser;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.core.thread.IIPThreadContextFactory;
import com.fiserv.isd.iip.core.thread.IIPUserContext;
import com.fiserv.isd.iip.core.util.StackTrace;
import com.stoneriver.iip.entconfig.batch.BatchLogConstants;
import com.stoneriver.iip.entconfig.batch.BatchLogDTO;
import com.stoneriver.iip.entconfig.batch.BatchLogDetailDTO;
import com.stoneriver.iip.entconfig.batch.BatchLogService;

public class BatchUtils {
	
	private static UserDetails details = getUserDetails();
	
	
	/**
	 * writes into batch log with message.
	 * @param stepExec
	 * @param message
	 * message to be logged
	 */
	public static void writeIntoBatchLog(StepExecution stepExec, String agreementTypeCode, String message) {
		BatchLogDTO batchLogDTO = new BatchLogDTO();
		batchLogDTO.setAgreementTypeCode(agreementTypeCode);
		batchLogDTO.setBatchLogTypeCode(BatchLogConstants.BATCH_PROCESS_INFO);
		batchLogDTO.setBatchJobTypeCode(stepExec.getJobExecution().getJobInstance().getJobName());
		batchLogDTO.setJobId(stepExec.getJobExecution().getId());
		batchLogDTO.setStepId(stepExec.getId());
		batchLogDTO.setDescription(message);
		batchLogDTO.setRunDate(DateUtility.getSystemDateTime());
		BatchLogDetailDTO batchLogDetailDTO = new BatchLogDetailDTO();
		batchLogDetailDTO.setCreateDate(DateUtility.getSystemDateTime());
		batchLogDetailDTO.setMessage(message);
		batchLogDTO.getBatchLogDetail().add(batchLogDetailDTO);
	    MuleServiceFactory.getService(BatchLogService.class).saveBatchLog(batchLogDTO);
	}
	
	/**
	 * writes error into batch log.
	 * @param stepExec
	 * @param message string
	 * @param exception exception details
	 */
	public static void writeIntoBatchLog(StepExecution stepExec, String agreementTypeCode, String message,
			Exception exception){
		//writing an error into batch log
		BatchLogDTO batch = new BatchLogDTO();
		batch.setAgreementTypeCode(agreementTypeCode);
		batch.setBatchJobTypeCode(stepExec.getJobExecution().getJobInstance().getJobName());
		batch.setBatchLogTypeCode(BatchLogConstants.BATCH_PROCESS_ERROR);
		batch.setJobId(stepExec.getJobExecution().getId());
		batch.setStepId(stepExec.getId());
		batch.setDescription(message);
		batch.setRunDate(DateUtility.getSystemDateTime());
		StackTrace st = new StackTrace(exception);
		BatchLogDetailDTO detail = new BatchLogDetailDTO();
		detail.setCreateDate(DateUtility.getSystemDateTime());
		detail.setMessage(st.toString());
		batch.getBatchLogDetail().add(detail);				
		MuleServiceFactory.getService(BatchLogService.class).saveBatchLog(batch);		
	}
	
	public static UserDetails getUserDetails(){
		return MuleServiceFactory.getService(UserDetailsService.class).
				loadUserByUsername(System.getProperty("batchuser")==null?"sysadmin":System.getProperty("batchuser"));
	}
	
	/*
	 * Setup Security Context
	 */
	
	public static void setupSecurityContext(){

		IIPUser iipUser = (IIPUser)details;
		IIPUserContext userContext = IIPThreadContextFactory.getUserContext();
		userContext.setId(iipUser.getUserId());
		
		//Creating User instance to set in Authentication object
		User userDetails = new User(details.getUsername(), details.getPassword(), iipUser.isEnabled(), 
				iipUser.isAccountNonExpired(), iipUser.isCredentialsNonExpired(), 
				iipUser.isAccountNonLocked(), iipUser.getAuthorities());
		
		UsernamePasswordAuthenticationToken login = 
			new UsernamePasswordAuthenticationToken(userDetails, details.getPassword(), iipUser.getAuthorities());
		
			SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL);
			SecurityContextHolder.getContext().setAuthentication(login);		
	}

}
