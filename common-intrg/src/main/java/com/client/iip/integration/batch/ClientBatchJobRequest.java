package com.client.iip.integration.batch;

import java.io.Serializable;

/**
 * This DTO Will Contain the request for Batch Job.
 * @author saurabh.bhatnagar
 *
 */
// Changes as per new requirement by Gaurav R. Removing asynchronous flag as request will always be asynchronous.
public class ClientBatchJobRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7595109179310387693L;
	
	private String jobName;
	private boolean runOnSystemDate;

	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}
	/**
	 * @param value the jobName to set
	 */
	public void setJobName(String value) {
		this.jobName = value;
	}
	/**
	 * @return the runOnSystemDate
	 */
	public boolean isRunOnSystemDate() {
		return runOnSystemDate;
	}
	/**
	 * @param value the runOnSystemDate to set
	 */
	public void setRunOnSystemDate(boolean value) {
		this.runOnSystemDate = value;
	}

}
