package com.client.iip.integration.reports;

import java.util.Collection;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.client.iip.integration.batch.IIPBatchTask;
import com.fiserv.isd.iip.core.date.DateUtility;


public class CLUEReportBatchTask extends IIPBatchTask implements Tasklet {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CLUEReportBatchTask.class);
	
	

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public RepeatStatus execute(StepContribution contribution,
			ChunkContext chunkContext) throws Exception {
		LOGGER.info("CLUEReportBatchTask Begin.....");
		//Obtain Job Variables
		JobParameters jobParameters = chunkContext.getStepContext().getStepExecution().getJobParameters();
		Date lastRunDate = jobParameters.getDate("lastRunDate");
		Date runDate = jobParameters.getDate("runDate");
		LOGGER.info("beginDate: " + lastRunDate);
		LOGGER.info("endDate: " + runDate);
		// Read Data
		Collection<CLUEReportDTO> reportDetails = this.readItems("client.claims.batch.cluereport", 
					com.client.iip.integration.reports.CLUEReportDTO.class, 
					"beginDate", DateUtility.toSQLTimestamp(lastRunDate), "endDate", DateUtility.toSQLTimestamp(runDate));
		LOGGER.info("Read Count :" + reportDetails.size());
		//Set read count.
		chunkContext.getStepContext().getStepExecution().setReadCount(reportDetails.size());
		
		//Set EndPoint Name
		setEndPointName("batchExportEndpoint");
		
		//Write Data
		this.writeItems(chunkContext.getStepContext().getJobName(), reportDetails);
		
		LOGGER.info("Write Count :" + reportDetails.size());
		//Set write count.
		chunkContext.getStepContext().getStepExecution().setWriteCount(reportDetails.size());
		
		LOGGER.info("CLUEReportBatchTask End.....");
		return RepeatStatus.FINISHED;
	}

}
