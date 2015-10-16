package com.client.iip.integration.sa;

import java.io.Serializable;


/**
 * 
 * @author Gaurav Rai
 *
 */
public class ControlNumberDTO implements Serializable {

	private static final long serialVersionUID = 721201240374930008L;

	private String controlNumber;
	private Long companyLOBId;
	private Long jurisdictionId;
	private String companyName;
	private Long companyId;
	private String jurisdictionName;

	public ControlNumberDTO() {
		super();
	}
	
	/**
	 * @return the controlNumber
	 */
	
	public String getControlNumber() {
		return controlNumber;
	}

	/**
	 * @param controlNumber
	 *            the controlNumber to set
	 */
	public void setControlNumber(String controlNumber) {
		this.controlNumber = controlNumber;
	}

	/**
	 * @return the companyLOBId
	 */
	
	public Long getCompanyLOBId() {
		return companyLOBId;
	}

	/**
	 * @param companyLOBId
	 *            the companyLOBId to set
	 */
	public void setCompanyLOBId(Long companyLOBId) {
		this.companyLOBId = companyLOBId;
	}

	/**
	 * @return the jurisdictionId
	 */
	
	public Long getJurisdictionId() {
		return jurisdictionId;
	}

	/**
	 * @param jurisdictionId
	 *            the jurisdictionId to set
	 */
	public void setJurisdictionId(Long jurisdictionId) {
		this.jurisdictionId = jurisdictionId;
	}
	/**
	 * 
	 * @return companyName
	 */
	public String getCompanyName() {
		return companyName;
	}
	/**
	 * 
	 * @param companyName
	 */
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	/**
	 * 
	 * @return companyId
	 */
	public Long getCompanyId() {
		return companyId;
	}
	/**
	 * 
	 * @param companyId
	 */
	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}
	/**
	 * 
	 * @return jurisdictionName
	 */
	public String getJurisdictionName() {
		return jurisdictionName;
	}
	/**
	 * 
	 * @param jurisdictionName
	 */
	public void setJurisdictionName(String jurisdictionName) {
		this.jurisdictionName = jurisdictionName;
	}
}
