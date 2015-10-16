package com.client.iip.integration.claim.imports;


import java.util.ArrayList;
import java.util.Collection;

import com.stoneriver.iip.workflow.api.WorkItemStatusCompositeDTO;
import com.stoneriver.iip.workflow.api.WorkItemStatusDTO;
/**
 * Client Replication of Composite DTO for Work Item Status.
 * 
 * @author Saurabh.Bhatnagar
 */

public class ClientWorkItemStatusCompositeDTO extends  WorkItemStatusCompositeDTO {

	private static final long serialVersionUID = -7233559186290749358L;
	private WorkItemStatusDTO workItemStatusDTO;
	private Collection<WorkItemStatusDTO> expiredWorkItemStatusCollection = new ArrayList<WorkItemStatusDTO>();
	
	
	public ClientWorkItemStatusCompositeDTO() {
		super();
	}

	public ClientWorkItemStatusCompositeDTO(WorkItemStatusCompositeDTO workItemStatus) {
		super();
		setWorkItemStatusDTO(workItemStatus.getCurrent());
		setExpiredWorkItemStatusCollection(workItemStatus.getExpired());
		setCurrent(null);
		setExpired(null);
	}

	/**
	 * @return the workItemStatusDTO
	 */
	public WorkItemStatusDTO getWorkItemStatusDTO() {
		return workItemStatusDTO;
	}

	/**
	 * @param value the workItemStatusDTO to set
	 */
	public void setWorkItemStatusDTO(WorkItemStatusDTO value) {
		this.workItemStatusDTO = value;
	}
	
	public WorkItemStatusDTO getCurrent(){
		return this.workItemStatusDTO;
	}

	/**
	 * @return the expiredWorkItemStatusCollection
	 */
	public Collection<WorkItemStatusDTO> getExpiredWorkItemStatusCollection() {
		return expiredWorkItemStatusCollection;
	}

	/**
	 * @param statusCollection the expiredWorkItemStatusCollection to set
	 */
	public void setExpiredWorkItemStatusCollection(
			Collection<WorkItemStatusDTO> statusCollection) {
		this.expiredWorkItemStatusCollection = statusCollection;
	}
	
	public Collection<WorkItemStatusDTO> getExpired() {
		return expiredWorkItemStatusCollection;
	}
	

}
