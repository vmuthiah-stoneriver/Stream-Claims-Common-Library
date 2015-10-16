package com.client.iip.integration.reports;

import java.math.BigDecimal;
import java.util.Date;

public class CLUEReportDTO {
	
	private String claimNumber;
	
	private Date lossDate;
	
	private String lossType;
	
	private String lossLocation;
	
	private BigDecimal paidAmount;
	
	private String insuredName;
	
	private String insuredAddressLine1;
	
	private String insuredAddressLine2;
	
	private String insuredCity;
	
	private String insuredState;
	
	private String insuredZip;

	public String getClaimNumber() {
		return claimNumber;
	}

	public void setClaimNumber(String claimNumber) {
		this.claimNumber = claimNumber;
	}

	public Date getLossDate() {
		return lossDate;
	}

	public void setLossDate(Date lossDate) {
		this.lossDate = lossDate;
	}

	public String getLossType() {
		return lossType;
	}

	public void setLossType(String lossType) {
		this.lossType = lossType;
	}

	public String getLossLocation() {
		return lossLocation;
	}

	public void setLossLocation(String lossLocation) {
		this.lossLocation = lossLocation;
	}

	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(BigDecimal paidAmount) {
		this.paidAmount = paidAmount;
	}

	public String getInsuredName() {
		return insuredName;
	}

	public void setInsuredName(String insuredName) {
		this.insuredName = insuredName;
	}

	public String getInsuredAddressLine1() {
		return insuredAddressLine1;
	}

	public void setInsuredAddressLine1(String insuredAddressLine1) {
		this.insuredAddressLine1 = insuredAddressLine1;
	}

	public String getInsuredAddressLine2() {
		return insuredAddressLine2;
	}

	public void setInsuredAddressLine2(String insuredAddressLine2) {
		this.insuredAddressLine2 = insuredAddressLine2;
	}

	public String getInsuredCity() {
		return insuredCity;
	}

	public void setInsuredCity(String insuredCity) {
		this.insuredCity = insuredCity;
	}

	public String getInsuredState() {
		return insuredState;
	}

	public void setInsuredState(String insuredState) {
		this.insuredState = insuredState;
	}

	public String getInsuredZip() {
		return insuredZip;
	}

	public void setInsuredZip(String insuredZip) {
		this.insuredZip = insuredZip;
	}

}
