package com.client.iip.integration.claim;


import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fiserv.isd.iip.core.audit.AuditData;

@Entity
@Table(name = "CLAIM_CONTROL_NUMBER_CD")
@AttributeOverrides({
	@AttributeOverride(name  = "recordId" , 
	column = @Column(name = "CLM_CTL_NO_ID" , columnDefinition = "numeric(19)"))
})
public class ControlNumberBO extends AuditData{

	private static final long serialVersionUID = -7393920808620141707L;
	private String controlNumber;
	private Long companyLOBId;
	private Long jurisdictionId;

	/**
	 * @return the controlNumber
	 */
	@Column(name = "CLM_CTL_NO")
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
	@Column(name = "CMPY_LOB_ID")
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
	@Column(name = "JUR_ID")
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

}
