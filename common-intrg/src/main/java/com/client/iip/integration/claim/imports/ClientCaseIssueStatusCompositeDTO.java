package com.client.iip.integration.claim.imports;

import java.util.ArrayList;
import java.util.Collection;

import com.stoneriver.iip.casetool.api.CaseIssueStatusCompositeDTO;
import com.stoneriver.iip.casetool.api.CaseIssueStatusDTO;

/**
 * @author vmuthiah
 *
 */
public class ClientCaseIssueStatusCompositeDTO extends
		CaseIssueStatusCompositeDTO {

	private static final long serialVersionUID = 12132012L;

	private CaseIssueStatusDTO caseIssueStatus;
	private Collection<CaseIssueStatusDTO> expiredCaseIssueStatusCollection = new ArrayList<CaseIssueStatusDTO>();
	
	public ClientCaseIssueStatusCompositeDTO(){
		super();
	}

	public ClientCaseIssueStatusCompositeDTO(CaseIssueStatusCompositeDTO caseIssuStatus){
		super();
		
		setCaseIssueStatus(caseIssuStatus.getCurrent());
		setExpiredCaseIssueStatusCollection(caseIssuStatus.getExpired());
		
		setExpired(null);
	}

	/**
	 * @return the caseIssueStatus
	 */
	public CaseIssueStatusDTO getCaseIssueStatus() {
		return caseIssueStatus;
	}
	/**
	 * @param caseIssueStatus the caseIssueStatus to set
	 */
	public void setCaseIssueStatus(CaseIssueStatusDTO caseIssueStatus) {
		this.caseIssueStatus = caseIssueStatus;
	}
	
	/* (non-Javadoc)
	 * @see com.stoneriver.iip.casetool.api.CaseIssueStatusCompositeDTO#getCurrent()
	 */
	public CaseIssueStatusDTO getCurrent() {
		return caseIssueStatus;
	}

	/**
	 * @return the expiredCaseIssueStatusCollection
	 */
	public Collection<CaseIssueStatusDTO> getExpiredCaseIssueStatusCollection() {
		return expiredCaseIssueStatusCollection;
	}
	/**
	 * @param expiredCaseIssueStatusCollection the expiredCaseIssueStatusCollection to set
	 */
	public void setExpiredCaseIssueStatusCollection(
			Collection<CaseIssueStatusDTO> expiredCaseIssueStatusCollection) {
		this.expiredCaseIssueStatusCollection = expiredCaseIssueStatusCollection;
	}
	/* (non-Javadoc)
	 * @see com.stoneriver.iip.casetool.api.CaseIssueStatusCompositeDTO#getExpired()
	 */
	public Collection<CaseIssueStatusDTO> getExpired() {
		return expiredCaseIssueStatusCollection;
	}
}
