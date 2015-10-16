package com.client.iip.integration.financials.disbursement;

import java.util.Calendar;
import java.util.Date;

public class ClientExportDisbursementsRequestDTO {
	//Set Default Start Date to Epoch
	private Date startDate = getDefaultDate();
	
	private Date endDate= new Date();
	
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	private Date getDefaultDate(){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(0);
		return cal.getTime();
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

}

