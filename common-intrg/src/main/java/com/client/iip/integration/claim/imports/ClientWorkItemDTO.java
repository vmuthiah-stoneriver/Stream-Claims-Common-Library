package com.client.iip.integration.claim.imports;


import com.stoneriver.iip.workflow.api.WorkItemDTO;
import com.stoneriver.iip.workflow.api.WorkItemStatusCompositeDTO;
/**
 * Client Replication of Work Item  DTO for Work Item.
 * 
 * @author Saurabh.Bhatnagar
 */

public class ClientWorkItemDTO extends  WorkItemDTO {
	
	private static final long serialVersionUID = 676169247233607091L;
	private ClientWorkItemStatusCompositeDTO clientWorkItemStatus;
	

	public ClientWorkItemDTO(){
		super();

	}
	public ClientWorkItemDTO(WorkItemDTO workItemIn){
		super();
		this.setAgreementData(workItemIn.getAgreementData());
		this.setAgreementIdDerived(workItemIn.getAgreementIdDerived());
		this.setAgreementSubTypeCodeDerived(workItemIn.getAgreementSubTypeCodeDerived());
		this.setAgreementSubTypeIdDerived(workItemIn.getAgreementSubTypeIdDerived());
		this.setAgreementTypeCodeDerived(workItemIn.getAgreementTypeCodeDerived());
		this.setChangesetId(workItemIn.getChangesetId());
		this.setChangesetStatusCd(workItemIn.getChangesetStatusCd());
		this.setChangesetStatusCd(workItemIn.getChangesetStatusCd());
		this.setCreatedDateTime(workItemIn.getCreatedDateTime());
		this.setCreatedUserDetails(workItemIn.getCreatedUserDetails());
		this.setEffectiveDateTime(workItemIn.getEffectiveDateTime());
		this.setEndDateTime(workItemIn.getEndDateTime());
		this.setExternalAgreementId(workItemIn.getExternalAgreementId());
		this.setExternalAgreementSubTypeId(workItemIn.getAgreementSubTypeCodeDerived());
		this.setExternalSourceId(workItemIn.getExternalSourceId());
		this.setFolder(workItemIn.getFolder());
		this.setInfoMsg(workItemIn.getInfoMsg());
		this.setObjectId(workItemIn.getObjectId());
		this.setObjectType(workItemIn.getObjectType());
		this.setRecordId(workItemIn.getRecordId());
		this.setRecordSourceCode(workItemIn.getRecordSourceCode());
		this.setSaveEvenIfRuleExist(workItemIn.getSaveEvenIfRuleExist());
		this.setUpdatedDateTime(workItemIn.getUpdatedDateTime());
		this.setUpdatedUserDetails(workItemIn.getUpdatedUserDetails());
		this.setVersion(workItemIn.getVersion());
		this.setWorkItemAssignment(workItemIn.getWorkItemAssignment());
		this.setWorkItemCategory(workItemIn.getWorkItemCategory());
		this.setWorkItemComment(workItemIn.getWorkItemComment());
		this.setWorkItemDate(workItemIn.getWorkItemDate());
		this.setWorkItemDescription(workItemIn.getWorkItemDescription());
		this.setWorkItemDueDate(workItemIn.getWorkItemDueDate());
		this.setWorkItemEscalation(workItemIn.getWorkItemEscalation());
		this.setWorkItemExternalIndexInfo(workItemIn.getWorkItemExternalIndexInfo());
		this.setWorkItemName(workItemIn.getWorkItemName());
		this.setWorkItemPriority(workItemIn.getWorkItemPriority());
		this.setWorkItemProcess(workItemIn.getWorkItemProcess());
		this.setWorkItemProcessId(workItemIn.getWorkItemProcessId());
		this.setWorkItemProcessStep(workItemIn.getWorkItemProcessStep());
		this.setWorkItemReoccurrence(workItemIn.getWorkItemReoccurrence());
		this.setWorkItemSource(workItemIn.getWorkItemSource());
		
		this.setClientWorkItemStatus(new ClientWorkItemStatusCompositeDTO(workItemIn.getWorkItemStatus()));
		
		this.setWorkItemStatus(null);
		
		this.setWorkItemSubCategory(workItemIn.getWorkItemSubCategory());
		this.setWorkItemTemplate(workItemIn.getWorkItemTemplate());
		this.setWorkItemType(workItemIn.getWorkItemType());
		
		this.setWorkItemRedirectOverrideIndicator(workItemIn.isWorkItemRedirectOverrideIndicator());
		this.setWorkItemAssignedUserModifiableIndicator(workItemIn.isWorkItemAssignedUserModifiableIndicator());
		this.setWorkItemCompleteCommentRequiredIndicator(workItemIn.isWorkItemCompleteCommentRequiredIndicator());
		this.setWorkItemConfidentialIndicator(workItemIn.isWorkItemConfidentialIndicator());
		this.setWorkItemEscalationExclPendingIndicator(workItemIn.isWorkItemEscalationExclPendingIndicator());
		this.setWorkItemEscalationToSupervisiorIndicator(workItemIn.isWorkItemEscalationToSupervisiorIndicator());
		this.setWorkItemEsclationIndicator(workItemIn.isWorkItemEsclationIndicator());
		this.setWorkItemPrivateIndicator(workItemIn.isWorkItemPrivateIndicator());
		this.setWorkItemOriginalIndicator(workItemIn.isWorkItemOriginalIndicator());
		this.setDeleted(workItemIn.isDeleted());
//		this.setUserLinkUpdateFlag(workItemIn.isUserLinkUpdateFlag());
	}
	
	/**
	 * @return the clientWorkItemStatus
	 */
	public ClientWorkItemStatusCompositeDTO getClientWorkItemStatus() {
		return clientWorkItemStatus;
	}

	/**
	 * @param value the clientWorkItemStatus to set
	 */
	public void setClientWorkItemStatus(
				ClientWorkItemStatusCompositeDTO value) {
		this.clientWorkItemStatus = value;
	}
	
	public WorkItemStatusCompositeDTO getWorkItemStatus(){
		return clientWorkItemStatus;
	}
	
}
