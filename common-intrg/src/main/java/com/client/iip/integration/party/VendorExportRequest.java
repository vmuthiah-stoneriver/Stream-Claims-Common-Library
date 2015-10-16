package com.client.iip.integration.party;

import java.io.Serializable;
import java.util.Date;

public class VendorExportRequest implements Serializable{

	private static final long serialVersionUID = -9130752008275913285L;
	private Date startDate;

	/**
	 * @return the startDate
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

}
