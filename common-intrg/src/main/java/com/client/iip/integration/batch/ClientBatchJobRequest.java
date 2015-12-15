package com.client.iip.integration.batch;

import java.io.Serializable;
import java.util.Date;

/**
 * This DTO Will Contain the request for Batch Job.
 * @author saurabh.bhatnagar
 *
 */
// Changes as per new requirement by Gaurav R. Removing asynchronous flag as request will always be asynchronous.
public class ClientBatchJobRequest implements Serializable{

	private static final long serialVersionUID = 7595109179310387693L;
	
	private String jobName;
	
	private boolean runOnSystemDate;
	
	private String runType;
	
	private String company;
	
	private String acctgPeriodMonth;
	
	private String acctgPeriodYr;
	
	private String acctgYearBasisCd;
	
	private Date busDate;

	/**
	 * @return the runType
	 */
	public String getRunType() {
		return runType;
	}
	/**
	 * @param runType the runType to set
	 */
	public void setRunType(String runType) {
		this.runType = runType;
	}
	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}
	/**
	 * @param company the company to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}
	/**
	 * @return the acctgPeriodMonth
	 */
	public String getAcctgPeriodMonth() {
		return acctgPeriodMonth;
	}
	/**
	 * @param acctgPeriodMonth the acctgPeriodMonth to set
	 */
	public void setAcctgPeriodMonth(String acctgPeriodMonth) {
		this.acctgPeriodMonth = acctgPeriodMonth;
	}
	/**
	 * @return the acctgPeriodYr
	 */
	public String getAcctgPeriodYr() {
		return acctgPeriodYr;
	}
	/**
	 * @param acctgPeriodYr the acctgPeriodYr to set
	 */
	public void setAcctgPeriodYr(String acctgPeriodYr) {
		this.acctgPeriodYr = acctgPeriodYr;
	}
	/**
	 * @return the acctgYearBasisCd
	 */
	public String getAcctgYearBasisCd() {
		return acctgYearBasisCd;
	}
	/**
	 * @param acctgYearBasisCd the acctgYearBasisCd to set
	 */
	public void setAcctgYearBasisCd(String acctgYearBasisCd) {
		this.acctgYearBasisCd = acctgYearBasisCd;
	}
	/**
	 * @return the busDate
	 */
	public Date getBusDate() {
		return busDate;
	}
	/**
	 * @param busDate the busDate to set
	 */
	public void setBusDate(Date busDate) {
		this.busDate = busDate;
	}
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
