package com.client.iip.integration.claim.imports;


import com.stoneriver.iip.casetool.api.CaseIssueDTO;
import com.stoneriver.iip.casetool.api.CaseIssueStatusCompositeDTO;

/**
 * @author vmuthiah
 *
 */
public class ClientCaseIssueDTO extends CaseIssueDTO {
	private static final long serialVersionUID = 12132012L;

	private ClientCaseIssueStatusCompositeDTO clientCaseIssueStatusColl = new ClientCaseIssueStatusCompositeDTO();

	/**
	 * @return the clientCaseIssueStatusColl
	 */
	public ClientCaseIssueStatusCompositeDTO getClientCaseIssueStatusColl() {
		return clientCaseIssueStatusColl;
	}

	/**
	 * @param caseIssueStatusColl the clientCaseIssueStatusColl to set
	 */
	public void setClientCaseIssueStatusColl(
			ClientCaseIssueStatusCompositeDTO caseIssueStatusColl) {
		this.clientCaseIssueStatusColl = caseIssueStatusColl;
	} 

	/**
	 * @return the clientCaseIssueStatusColl
	 */
	public CaseIssueStatusCompositeDTO getCaseIssueStatusColl() {
		return clientCaseIssueStatusColl;
	}
}
