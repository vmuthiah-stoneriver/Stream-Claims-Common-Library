package com.client.iip.integration.claim.details;

import com.stoneriver.iip.assignment.search.ViewAssignmentResultDTO;

/**
 * @author vmuthiah
 *
 */
public class ClientViewAssignmentResultDTO extends ViewAssignmentResultDTO {

	private static final long serialVersionUID = 8202012L;
	
	private String userFaxNumber;
	private String supervisorFaxNumber;
	
	/**
	 * @return the userFaxNumber
	 */
	public String getUserFaxNumber() {
		return userFaxNumber;
	}
	
	/**
	 * @param userFaxNumber the userFaxNumber to set
	 */
	public void setUserFaxNumber(String userFaxNumber) {
		this.userFaxNumber = userFaxNumber;
	}
	
	/**
	 * @return the supervisorFaxNumber
	 */
	public String getSupervisorFaxNumber() {
		return supervisorFaxNumber;
	}
	
	/**
	 * @param supervisorFaxNumber the supervisorFaxNumber to set
	 */
	public void setSupervisorFaxNumber(String supervisorFaxNumber) {
		this.supervisorFaxNumber = supervisorFaxNumber;
	}
}
