package com.client.iip.integration.claim.imports;

import com.stoneriver.iip.assignment.api.AssignmentStatusCompositeDTO;
import com.stoneriver.iip.assignment.api.ImportAssignmentDTO;

/**
 * Client Replication of Import Assignment  DTO for Assignment.
 * 
 * @author Saurabh.Bhatnagar
 */

public class ClientImportAssignmentDTO extends ImportAssignmentDTO {
	
	/**
	 * Unique Serial version Id.
	 */
	private static final long serialVersionUID = 10082012L;	 

	private ClientAssignmentStatusCompositeDTO clientAssignmentStatus;
	private String userFaxNumber;
	private String supervisorFaxNumber;
	private String userFirstName;
	private String userMiddleName;
	private String userLastName;
	private String supervisorPhoneNumber;
	

	public ClientImportAssignmentDTO(){
		super();
	}

	/**
	 * @return the clientAssignmentStatus
	 */
	public ClientAssignmentStatusCompositeDTO getClientAssignmentStatus() {
		return clientAssignmentStatus;
	}

	/**
	 * @param value the clientAssignmentStatus to set
	 */
	public void setClientAssignmentStatus(
			ClientAssignmentStatusCompositeDTO value) {
		this.clientAssignmentStatus = value;
	}
	
	/**
	 * @return the assignmentStatus
	 */
	public AssignmentStatusCompositeDTO getAssignmentStatus() {
		return clientAssignmentStatus;
	}

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

	/**
	 * @return the userFirstName
	 */
	public String getUserFirstName() {
		return userFirstName;
	}

	/**
	 * @param value the userFirstName to set
	 */
	public void setUserFirstName(String value) {
		this.userFirstName = value;
	}

	/**
	 * @return the userLastName
	 */
	public String getUserLastName() {
		return userLastName;
	}

	/**
	 * @param value the userLastName to set
	 */
	public void setUserLastName(String value) {
		this.userLastName = value;
	}

	/**
	 * @return the userMiddleName
	 */
	public String getUserMiddleName() {
		return userMiddleName;
	}

	/**
	 * @param value the userMiddleName to set
	 */
	public void setUserMiddleName(String value) {
		this.userMiddleName = value;
	}

	/**
	 * @return the supervisorPhoneNumber
	 */
	public String getSupervisorPhoneNumber() {
		return supervisorPhoneNumber;
	}

	/**
	 * @param value the supervisorPhoneNumber to set
	 */
	public void setSupervisorPhoneNumber(String value) {
		this.supervisorPhoneNumber = value;
	}
}
