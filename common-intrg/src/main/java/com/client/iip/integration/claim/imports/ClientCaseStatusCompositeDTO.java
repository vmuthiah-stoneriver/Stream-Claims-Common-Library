package com.client.iip.integration.claim.imports;

import java.util.ArrayList;
import java.util.Collection;

import com.stoneriver.iip.casetool.api.CaseStatusCompositeDTO;
import com.stoneriver.iip.casetool.api.CaseStatusDTO;
/**
 * Client Replication of Composite DTO for Case Status.
 * 
 * @author Saurabh.Bhatnagar
 */

public class ClientCaseStatusCompositeDTO extends  CaseStatusCompositeDTO {

	private static final long serialVersionUID = -1955119786820060124L;

	private CaseStatusDTO caseStatusDTO;
	private Collection<CaseStatusDTO> expiredCaseStatusCollection = new ArrayList<CaseStatusDTO>();

	public ClientCaseStatusCompositeDTO(){
		super();
	}

	public ClientCaseStatusCompositeDTO(CaseStatusCompositeDTO caseStatus){
		super();
		
		setCaseStatusDTO(caseStatus.getCurrent());
		setExpiredCaseStatusCollection(caseStatus.getExpired());
		setCaseStatusReasons(null);
		setCaseActionStatusCodeMap(null);
		setCaseResolutions(null);
		setCaseActions(null);
		
		setNextStatusReasonCode(caseStatus.getNextStatusReasonCode());
		setCaseCompleteDate(caseStatus.getCaseCompleteDate());
		setCaseResolutionCode(caseStatus.getCaseResolutionCode());
		
		setExpired(null);
	}

	/**
	 * @return the caseStatusDTO
	 */
	public CaseStatusDTO getCaseStatusDTO() {
		return caseStatusDTO;
	}

	/**
	 * @param caseStatusDTO the caseStatusDTO to set
	 */
	public void setCaseStatusDTO(CaseStatusDTO caseStatusDTO) {
		this.caseStatusDTO = caseStatusDTO;
	}

	public CaseStatusDTO getCurrent() {
		return caseStatusDTO;
	}

	/**
	 * @return the expiredCaseStatusCollection
	 */
	public Collection<CaseStatusDTO> getExpiredCaseStatusCollection() {
		return expiredCaseStatusCollection;
	}

	/**
	 * @param expiredCollection the expiredCaseStatusCollection to set
	 */
	public void setExpiredCaseStatusCollection(
			Collection<CaseStatusDTO> expiredCollection) {
		this.expiredCaseStatusCollection = expiredCollection;
	}

	public Collection<CaseStatusDTO> getExpired() {
		return expiredCaseStatusCollection;
	}
	
}
