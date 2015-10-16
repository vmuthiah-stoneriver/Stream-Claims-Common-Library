package com.client.iip.integration.claim.imports;

import java.util.ArrayList;
import java.util.Collection;

import com.stoneriver.iip.casetool.api.CaseDTO;
import com.stoneriver.iip.casetool.api.CaseIssueDTO;
import com.stoneriver.iip.casetool.api.CaseStatusCompositeDTO;

/**
 * Client Replication of Case  DTO for Work Item.
 * 
 * @author Saurabh.Bhatnagar
 */

public class ClientCaseDTO extends CaseDTO {
	
	private static final long serialVersionUID = -6544825401032045539L;
	private ClientCaseStatusCompositeDTO clientCaseStatus;
	private Collection<ClientCaseIssueDTO> clientCaseIssueColl = new ArrayList<ClientCaseIssueDTO>();	

	public ClientCaseDTO(){
		super();
	}
	
	/**
	 * @return the clientCaseStatus
	 */
	public ClientCaseStatusCompositeDTO getClientCaseStatus() {
		return clientCaseStatus;
	}

	/**
	 * @param clientCaseStatus the clientCaseStatus to set
	 */
	public void setClientCaseStatus(ClientCaseStatusCompositeDTO clientStatus) {
		this.clientCaseStatus = clientStatus;
	}

	/**
	 * @return the status
	 */
	public CaseStatusCompositeDTO getCaseStatus() {
		return clientCaseStatus;
	}

	/**
	 * @return the clientCaseIssueColl
	 */
	public Collection<ClientCaseIssueDTO> getClientCaseIssueColl() {
		return clientCaseIssueColl;
	}

	/**
	 * @param clientCaseIssueColl the clientCaseIssueColl to set
	 */
	public void setClientCaseIssueColl(
			Collection<ClientCaseIssueDTO> caseIssueColl) {
		this.clientCaseIssueColl = caseIssueColl;
	}
	/**
	 * @return the clientCaseIssueColl
	 */
	public Collection<CaseIssueDTO> getCaseIssueColl() {
		Collection<CaseIssueDTO> caseIssueColl = new ArrayList<CaseIssueDTO>();
		if(clientCaseIssueColl != null) {
			for(ClientCaseIssueDTO clientCaseIssue : clientCaseIssueColl) {
				caseIssueColl.add(clientCaseIssue);
			}
		}
		return caseIssueColl;
	}
}
