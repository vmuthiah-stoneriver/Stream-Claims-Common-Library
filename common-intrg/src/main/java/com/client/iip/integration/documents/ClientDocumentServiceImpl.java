package com.client.iip.integration.documents;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Service;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.stoneriver.iip.document.DocumentBO;
import com.stoneriver.iip.document.DocumentConstants;
import com.stoneriver.iip.document.DocumentDAO;
import com.stoneriver.iip.document.api.DocumentElectronicMediaFileDTO;
import com.stoneriver.iip.document.api.DocumentOutboundDTO;
import com.stoneriver.iip.document.api.DocumentService;
import com.stoneriver.iip.document.bo.DocumentExternalIndexBO;
import com.stoneriver.iip.document.recipient.api.DocumentRecipientDeliverResult;
import com.stoneriver.iip.entconfig.api.CompanyOrganizationCriteriaDTO;
import com.stoneriver.iip.entconfig.api.EnterpriseConfigService;
import com.stoneriver.iip.entconfig.sa.api.company.CompanyDetailsSummaryDTO;
import com.stoneriver.iip.entconfig.tool.ContextDataListingDTO;
import com.stoneriver.iip.entconfig.tool.EntconfigToolSupportService;
import com.stoneriver.iip.entconfig.tool.ToolContextRequestDTO;

/**
 *  Document Delivery Composite service.
 * 
 * @author saurabh.bhatnagar
 *
 */
@Service(id = "integration.serviceObject.ClientDocumentService")
public class ClientDocumentServiceImpl implements ClientDocumentService{
	
	private Logger logger = LoggerFactory.getLogger(ClientDocumentServiceImpl.class);
	private DocumentService documentService;
	private ClientDocumentHelper clientDocumentHelper;
	private EnterpriseConfigService enterpriseConfigService;
	private DocumentDAO documentDAO;
	private EntconfigToolSupportService entconfigToolSupportService;
	
	/**
	 * @return the documentDAO
	 */
	public DocumentDAO getDocumentDAO() {
		return documentDAO;
	}
	/**
	 * @param dao the documentDAO to set
	 */
	@Inject(DaoInterface="document.daointerface.documentDao")
	public void setDocumentDAO(DocumentDAO dao) {
		this.documentDAO = dao;
	}
	
	/**
	 * @return the enterpriseConfigService
	 */
	public EnterpriseConfigService getEnterpriseConfigService() {
		if(enterpriseConfigService == null) {
			enterpriseConfigService = MuleServiceFactory.getService(EnterpriseConfigService.class);
		}
		return enterpriseConfigService;
	}
	
	
	/**
	 * @return the entconfigToolSupportService
	 */
	public EntconfigToolSupportService getEntconfigToolSupportService() {
		if(entconfigToolSupportService == null) {
			entconfigToolSupportService = MuleServiceFactory.getService(EntconfigToolSupportService.class);
		}
		return entconfigToolSupportService;
	}
	
	
	/**
	 * @return the clientDocumentHelper
	 */
	public ClientDocumentHelper getClientDocumentHelper() {
		return clientDocumentHelper;
	}
	
	/**
	 * @param helper the clientDocumentHelper to set
	 */
	@Inject(PojoRef = "client.document.pojo.clientDocumentHelper")
	public void setClientDocumentHelper(ClientDocumentHelper helper) {
		this.clientDocumentHelper = helper;
	}

	/**
	 * @return the documentService
	 */
	public DocumentService getDocumentService() {
		if(this.documentService == null){
			this.documentService = MuleServiceFactory.getService(DocumentService.class);
		}
		return documentService;
	}
	/**
	 * This method will deliver the document to recipient. 
	 * @param request DocumentRecipientDeliverResult
	 */
	@Transactional
	public String updateDocumentStatus(Collection<ClientDocumentRecipientDeliverResult> requestList){
		String response = null;
		Map<Long,DocumentOutboundDTO> documentMap  = new HashMap<Long, DocumentOutboundDTO>();
		Map<Long, Map<Long, DocumentRecipientDeliverResult>> deliveryResultMap = new HashMap<Long, Map<Long, DocumentRecipientDeliverResult>>();
		for(ClientDocumentRecipientDeliverResult request : requestList){
			if(request.getDocumentId()!=null){
				logger.info("Calling Helper Class to get the DocumentOutboundDTO from Document Id - {} ", request.getDocumentId());
				DocumentOutboundDTO outboundDTO = this.getDocumentService().retrieveOutboundDocument(request.getDocumentId());
				if(outboundDTO.getRecordId()!=null){
					documentMap.put(request.getDocumentId(),outboundDTO);
					Map<Long, DocumentRecipientDeliverResult> tempResultMap = new HashMap<Long, DocumentRecipientDeliverResult>();
					for(DocumentRecipientType recipientType : request.getRecipients()){
						DocumentRecipientDeliverResult deliverResult = new DocumentRecipientDeliverResult();
						deliverResult.setDocumentId(request.getDocumentId());
						deliverResult.setRecipientPartyId(recipientType.getRecipientRecordId());
						deliverResult.setSentDate(recipientType.getSentDate());
						deliverResult.setSentStatus(recipientType.getSentStatus());
						deliverResult.setSentToDevice(recipientType.getSentToDevice());
						tempResultMap.put(recipientType.getRecipientRecordId(), deliverResult);			
					}
					deliveryResultMap.put(request.getDocumentId(), tempResultMap);
				}
			} 
		}
		clientDocumentHelper.confirmDocumentDelivery(deliveryResultMap, documentMap);
		response = "Document(s) processed successfully";
		return response;
	}

	@Override
	public void createNewInboundAttachment(ClientDocumentDTO document) {
		DocumentElectronicMediaFileDTO mf = new DocumentElectronicMediaFileDTO();
		BeanUtils.copyProperties(document, mf);
		mf.setDocumentSavedAs(DocumentConstants.DOCUMENT_IMAGING_FILE);
		String fileName = document.getDocumentFileName()==null?document.getDocumentReferenceNumber():document.getDocumentFileName();

		if(document.getDocumentFileLocationCode().equals(DocumentConstants.DOCUMENT_NETWORK_FILE)
				|| (document.getDocumentFileName() != null && document.getDocumentFileName().contains("."))){
			mf.setDocumentSavedAs(DocumentConstants.DOCUMENT_NETWORK_FILE);
		}
		mf.setMediaFileName(document.getDocumentName());
		//Set the File name for network files
		if(mf.getDocumentSavedAs().equals(DocumentConstants.DOCUMENT_NETWORK_FILE)){
			mf.setFile(fileName);
		}
		mf.setMediaFileDesc(document.getDocumentDescription());
		DocumentService documentService = MuleServiceFactory.getService(DocumentService.class);
		DocumentElectronicMediaFileDTO mediaFileDTO = documentService.saveDocumentElectronicMediaFile(mf);
		//Save External Index for image documents
		if(mf.getDocumentSavedAs().equals(DocumentConstants.DOCUMENT_IMAGING_FILE)){
			mediaFileDTO.setDocmImageId(document.getDocumentFileName());
			saveDocumentExternalIndex(mediaFileDTO);
		}
	}
	
	/**
	 * This API will create/save the DocumentExternalIndexDTO from DocumentDTO. 
	 * @param documentDTO DocumentDTO
	 * @return DocumentExternalIndexDTO
	 */
	public void saveDocumentExternalIndex(DocumentElectronicMediaFileDTO documentDTO){
		DocumentExternalIndexBO documentExternalIndexBO=new DocumentExternalIndexBO();
		populateDocumentExternalIndexDetails(documentDTO, documentExternalIndexBO);
		DocumentBO documentBO =  documentDAO.retrieveDocument(documentDTO.getRecordId());
		documentExternalIndexBO.setDocument(documentBO);
		documentDAO.saveDocumentExternalIndex(documentExternalIndexBO);
	}

	/**
	 * This method populates the Document External Index object from the DocumentDTO.
	 * 
	 * @param documentDTO
	 * @param documentExternalIndexBO
	 */
	public void populateDocumentExternalIndexDetails(DocumentElectronicMediaFileDTO documentDTO, DocumentExternalIndexBO documentExternalIndexBO) {
		documentExternalIndexBO.setContextRecordSourceCode(documentDTO.getRecordSourceCode());
		documentExternalIndexBO.setDocumentImageId(documentDTO.getDocmImageId());
		ToolContextRequestDTO contextRequestDTO = new ToolContextRequestDTO();
		contextRequestDTO.setContextId(documentDTO.getAgreementIdDerived());
		contextRequestDTO.setContextType(documentDTO.getAgreementTypeCodeDerived());
		ContextDataListingDTO dataListingDTO  = getEntconfigToolSupportService().retrieveContextListing(contextRequestDTO);
		if(dataListingDTO!=null){
			documentExternalIndexBO.setContextReferenceNumber(dataListingDTO.getContextNo());
			documentExternalIndexBO.setContextTypeCode(dataListingDTO.getContextTypeCode());
			documentExternalIndexBO.setSubContextRefNo(String.valueOf(documentDTO.getAgreementSubIdDerived()));
			documentExternalIndexBO.setSubContextTypeCode(documentDTO.getAgreementSubTypeCodeDerived());
			if(dataListingDTO.getContextTypeCode()!=null){
			    documentExternalIndexBO.setContextTypeName(
			      getEnterpriseConfigService().retrieveAgreementTypeName(dataListingDTO.getContextTypeCode()));
			   }
		}else {
			documentExternalIndexBO.setContextReferenceNumber(String.valueOf(documentDTO.getAgreementIdDerived()));
			documentExternalIndexBO.setContextTypeCode(documentDTO.getAgreementTypeCodeDerived());
			documentExternalIndexBO.setSubContextRefNo(String.valueOf(documentDTO.getAgreementSubIdDerived()));
			documentExternalIndexBO.setSubContextTypeCode(documentDTO.getAgreementSubTypeCodeDerived());
			if(documentDTO.getAgreementTypeCodeDerived()!=null){
				documentExternalIndexBO.setContextTypeName(
						getEnterpriseConfigService().retrieveAgreementTypeName(documentDTO.getAgreementTypeCodeDerived()));
			}
		}
		// Retrieve Company And Corporation Details
		if(documentDTO.getCompanyIdDerived()!=null){
			CompanyOrganizationCriteriaDTO criteriaDTO = new CompanyOrganizationCriteriaDTO();
			criteriaDTO.setCmpyId(documentDTO.getCompanyIdDerived());
			CompanyDetailsSummaryDTO companyDetailsSummaryDTO = getEnterpriseConfigService().retrieveCompanyDetails(criteriaDTO);
			if(companyDetailsSummaryDTO!=null){
				documentExternalIndexBO.setCorporationName(companyDetailsSummaryDTO.getCorpName());
				documentExternalIndexBO.setCorporationNumber(companyDetailsSummaryDTO.getCorpId()!=null 
						? companyDetailsSummaryDTO.getCorpId().toString() : null);
				documentExternalIndexBO.setCompanyName(companyDetailsSummaryDTO.getCompName());
				documentExternalIndexBO.setCompanyNumber(companyDetailsSummaryDTO.getCompId()!=null
						? companyDetailsSummaryDTO.getCompId().toString() : null);
			}
		}
		documentExternalIndexBO.setDocumentDate(documentDTO.getDocumentCreationDate());
		documentExternalIndexBO.setDocumentTypeCode(documentDTO.getSourceCode());
		//Setting Context Reference Effective Date with document creation date. 
		documentExternalIndexBO.setContextReferenceEffDate(documentDTO.getDocumentCreationDate());
		if(documentDTO.getAgreementSubIdDerived()!=null){
		   documentExternalIndexBO.setSubContextRecordSourceCode(documentDTO.getRecordSourceCode());
		   documentExternalIndexBO.setSubContextRefEffDate(documentDTO.getDocumentCreationDate());
		}
	}	
}
