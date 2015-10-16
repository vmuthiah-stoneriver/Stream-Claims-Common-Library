package com.client.iip.integration.claim.imports;

import java.util.ArrayList;
import java.util.Collection;

import com.stoneriver.iip.assignment.api.AssignmentStatusCompositeDTO;
import com.stoneriver.iip.assignment.api.AssignmentStatusDTO;
/**
 * Client Replication of Composite DTO for Assignment Status.
 * 
 * @author Saurabh.Bhatnagar
 */

public class ClientAssignmentStatusCompositeDTO extends  AssignmentStatusCompositeDTO {

	
	private static final long serialVersionUID = -5584160861613781830L;
	private AssignmentStatusDTO assignmentStatusDTO;
	private Collection<AssignmentStatusDTO> expiredAssignmentStatusCollection = new ArrayList<AssignmentStatusDTO>();
	
	
	public ClientAssignmentStatusCompositeDTO(AssignmentStatusCompositeDTO assignmentStatus) {
		super();
		setAssignmentStatusDTO(assignmentStatus.getCurrent());
		setExpiredAssignmentStatusCollection(assignmentStatus.getExpired());
		setCurrent(null);
		setExpired(null);
	}
	
	
	public ClientAssignmentStatusCompositeDTO(){
		super();
		
	}

	/**
	 * @return the assignmentStatusDTO
	 */
	public AssignmentStatusDTO getAssignmentStatusDTO() {
		return assignmentStatusDTO;
	}

	/**
	 * @param value the assignmentStatusDTO to set
	 */
	public void setAssignmentStatusDTO(AssignmentStatusDTO value) {
		this.assignmentStatusDTO = value;
	}

	/**
	 * @return current Status DTO.
	 */
	public AssignmentStatusDTO getCurrent() {
		return assignmentStatusDTO;
	}
	
	/**
	 * @return the expiredAssignmentCollection
	 */
	public Collection<AssignmentStatusDTO> getExpiredAssignmentStatusCollection() {
		return expiredAssignmentStatusCollection;
	}

	/**
	 * @param statusCollection the expiredWorkItemStatusCollection to set
	 */
	public void setExpiredAssignmentStatusCollection(
			Collection<AssignmentStatusDTO> statusCollection) {
		this.expiredAssignmentStatusCollection = statusCollection;
	}
	
	public Collection<AssignmentStatusDTO> getExpired() {
		return expiredAssignmentStatusCollection;
	}

}
