package com.client.iip.integration.documents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.fiserv.isd.iip.bc.address.api.PartyCorrespondenceDTO;
import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.service.MuleServiceFactory;
import com.fiserv.isd.iip.util.codelookup.api.CodeLookupBO;
import com.stoneriver.iip.document.DocumentConstants;
import com.stoneriver.iip.document.DocumentServiceHelper;
import com.stoneriver.iip.document.api.DocumentDTO;
import com.stoneriver.iip.document.api.DocumentElectronicMediaFileDTO;
import com.stoneriver.iip.document.api.DocumentOutboundDTO;
import com.stoneriver.iip.document.api.DocumentRenderException;
import com.stoneriver.iip.document.api.DocumentService;
import com.stoneriver.iip.document.recipient.api.DocumentOutboundRecipientDTO;
import com.stoneriver.iip.document.render.CompanyRelatedInformation;
import com.stoneriver.iip.document.render.DocumentRenderRequest;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionAssociationDTO;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionSearchCriteria;
import com.stoneriver.iip.entconfig.systemoption.SystemOptionService;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Client Document helper.
 * 
 * @author sudharsan.sriram
 * 
 */
@Pojo(id = "client.document.pojo.clientDocumentHelper")
public class ClientDocumentHelper extends DocumentServiceHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDocumentHelper.class);
	private static final String FREEMARKER_CONFIG_ID = "document.config.freemarkerConfiguration";
	private static final String DELIVER_TEMPLATE = "docmoutbnd_deliver.ftl";

	private static final String DELIVER_PRINTER_SYSOPTION = "printer_name";
	private static final String DELIVER_EMAIL_SYSOPTION = "email_server";
	private static final String DELIVER_FAX_SYSOPTION = "fax_machine";
	
	private Configuration config;
	private String printerCode;

	private DocumentService documentService;

	/**
	 * @return the config
	 */
	public Configuration getConfig() {
		return config;
	}

	/**
	 * @param configuration
	 *            the config to set
	 */
	@Inject(PojoRef = FREEMARKER_CONFIG_ID)
	public void setConfig(Configuration configuration) {
		this.config = configuration;
	}

	/**
	 * Method to deliver the document using the client. This method created the
	 * necessary XML schema which goes out to the client s/w for delivering the
	 * document.
	 * 
	 * @param documents
	 *            List of outbound documents to deliver.
	 * @param printerCd
	 *            code for selected printer.
	 * @return deliveryResult delivery result.
	 */
	public DocumentRenderRequest deliverDocuments(Collection<DocumentDTO> documents, String printerCd) {
		Template t = null;
		this.printerCode = printerCd;
		StringBuffer xmlToDeliver = new StringBuffer();
		try {
			t = getConfig().getTemplate(DELIVER_TEMPLATE);
			xmlToDeliver.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><outboundDocuments>");

			for (DocumentDTO document : documents) {
				HashMap<String, Object> model = new HashMap<String, Object>();
				model.put("document", document);

				PartyCorrespondenceDTO companyAddressInfo = null;
				Collection<PartyCorrespondenceDTO> companyPhoneNumbers = new ArrayList<PartyCorrespondenceDTO>();
				PartyCorrespondenceDTO companyEmailAddress = new PartyCorrespondenceDTO();
				PartyCorrespondenceDTO companyWebSite = new PartyCorrespondenceDTO();
				CompanyRelatedInformation companyRelatedInfo = new CompanyRelatedInformation();

				if (document.getCompanyIdDerived() != null) {

					companyAddressInfo = populateCompanyAddressInformation(document.getCompanyIdDerived());
					// Get phone numbers of company //Communication method phn
					Collection<PartyCorrespondenceDTO> interationChannels = getDocumentDAO()
							.retrieveCompanyInteractionChannels(document.getCompanyIdDerived());
					for (PartyCorrespondenceDTO pcd : interationChannels) {
						if (DocumentConstants.COMMUNICATION_METHOD_PHONE.equalsIgnoreCase(pcd
								.getCorrespondeceTypeCode())) {
							companyPhoneNumbers.add(pcd);
						} else if (DocumentConstants.COMMUNICATION_METHOD_EMAIL.equalsIgnoreCase(pcd
								.getCorrespondeceTypeCode())) {
							// There will be only one in CD table and template
							// structure also.
							// Get the last object as most effective added order
							// by in query.
							companyEmailAddress = pcd;
						} else if (DocumentConstants.COMMUNICATION_METHOD_OTHER.equalsIgnoreCase(pcd
								.getCorrespondeceTypeCode())) {
							// There will be only one in CD table and template
							// structure also
							// get the last object as most effective added order
							// by in query.
							companyWebSite = pcd;
						}
					}
					// get company related information
					companyRelatedInfo = populateCompanyRelatedInformation(document.getCompanyIdDerived());
				}

				model.put("companyAddress", companyAddressInfo);
				model.put("companyPhoneNumbers", companyPhoneNumbers);
				model.put("companyEmailAddress", companyEmailAddress);
				model.put("companyWebSite", companyWebSite);
				model.put("companyRelatedInfo", companyRelatedInfo);

				// Recipients need to be put into the model
				Collection<DocumentOutboundRecipientDTO> recipients = ((DocumentOutboundDTO) document)
						.getDocumentRecipientCompositeCollection();
				DocumentOutboundRecipientDTO toRecipient = null;
				Collection<DocumentOutboundRecipientDTO> ccRecipients = new ArrayList<DocumentOutboundRecipientDTO>();
				// Iterate over allRecipients and split them into other
				// variables
				for (DocumentOutboundRecipientDTO recip : recipients) {
					if (recip.getDocumentRecipientDeliveryTypeCode() != null
							&& DocumentConstants.DOCUMENT_DELIVERY_TYPE_CODE_MAIL.equals(recip
									.getDocumentRecipientDeliveryTypeCode())) {
						if (recip.getRecipientMailDelivery() != null) {
							populateRecipientMailDeliveryInformation(recip);
						}
					}
					if (DocumentConstants.DOCUMENT_RECIPIENT_RECIEVER_TYPE_CODE_TO.equals(recip
							.getDocumentRecipientRecieverTypeCode())) {
						toRecipient = recip;
					} else if (DocumentConstants.DOCUMENT_RECIPIENT_RECIEVER_TYPE_CODE_CC.equals(recip
							.getDocumentRecipientRecieverTypeCode())) {
						ccRecipients.add(recip);
					}
				}
				model.put("toRecipient", toRecipient);
				try {
					if (toRecipient != null) {
						((DocumentOutboundDTO) document).setDeliverTypeCode(toRecipient
								.getDocumentRecipientDeliveryTypeCode());
					}
					model.put("document", document);
					if (toRecipient != null) {
						model.put(
								"deviceName",
								getSystemOptionDeviceName(toRecipient.getDocumentRecipientDeliveryTypeCode(),
										document.getCompanyIdDerived()));
					}
					xmlToDeliver.append(FreeMarkerTemplateUtils.processTemplateIntoString(t, model));
					model.remove("toRecipient");
					for (DocumentOutboundRecipientDTO ccRecipient : ccRecipients) {
						((DocumentOutboundDTO) document).setDeliverTypeCode(ccRecipient
								.getDocumentRecipientDeliveryTypeCode());
						model.put("document", document);
						model.put(
								"deviceName",
								getSystemOptionDeviceName(ccRecipient.getDocumentRecipientDeliveryTypeCode(),
										document.getCompanyIdDerived()));
						model.put("ccRecipient", ccRecipient);
						xmlToDeliver.append(FreeMarkerTemplateUtils.processTemplateIntoString(t, model));
					}
				} catch (TemplateException e) {
					throw new DocumentRenderException("Unable to process template into string. Document: " + document
							+ " Template: " + t, e);
				}
			}
			xmlToDeliver.append("</outboundDocuments>");
			LOGGER.debug("--------- Deliver Document XML ------------ {}", xmlToDeliver);
		} catch (IOException e) {
			throw new DocumentRenderException("No template name matches  '" + DELIVER_TEMPLATE + "'.", e);
		}

		DocumentRenderRequest request = new DocumentRenderRequest();
		request.setXml(xmlToDeliver.toString());
		return request;
	}

	/**
	 * this method is used to populate company Address information.
	 * 
	 * @param companyId
	 *            CompanyId
	 * @return PartyCorrespondenceDTO
	 */
	private PartyCorrespondenceDTO populateCompanyAddressInformation(Long companyId) {
		PartyCorrespondenceDTO companyAddressInfo = new PartyCorrespondenceDTO();
		Collection<PartyCorrespondenceDTO> partyCorrespondance = getDocumentDAO().retrieveCompanyAddressInformation(
				companyId, DocumentConstants.DOCUMENT_DELIVERY_TYPE_CODE_MAIL);
		// Get the most effective if more than one exist
		companyAddressInfo = (partyCorrespondance != null && partyCorrespondance.size() > 0) ? partyCorrespondance
				.iterator().next() : null;

		return companyAddressInfo;
	}

	/**
	 * this method is used to populate company related information.
	 * 
	 * @param companyId
	 *            CompanyId
	 * @return CompanyRelatedInformation
	 */
	private CompanyRelatedInformation populateCompanyRelatedInformation(Long companyId) {
		CompanyRelatedInformation companyInfo = new CompanyRelatedInformation();
		Collection<CompanyRelatedInformation> companyRelatedInfo = getDocumentDAO().retrieveCompanyRelatedInformation(
				companyId);
		companyInfo = (companyRelatedInfo != null && companyRelatedInfo.size() > 0) ? companyRelatedInfo.iterator()
				.next() : companyInfo;

		return companyInfo;
	}

	/**
	 * This method is used to add code/names for state and country. in case if
	 * mail delivery for recipient.
	 * 
	 * @param recip
	 *            DocumentOutboundRecipientDTO
	 */
	private void populateRecipientMailDeliveryInformation(DocumentOutboundRecipientDTO recip) {
		if (recip.getRecipientMailDelivery().getPostalServiceRegionId() != null) {
			Collection<CodeLookupBO> cdos = getDocumentDAO().retrieveDocumentRecipientPostalCodeAndName(
					recip.getRecipientMailDelivery().getPostalServiceRegionId());
			// There is only one record so get first.
			CodeLookupBO cdo = (cdos != null && cdos.size() > 0) ? cdos.iterator().next() : null;
			recip.getRecipientMailDelivery().setDocumentRecipientPostalSvcRegionCode(
					cdo != null ? cdo.getCode().toString() : "");
			recip.getRecipientMailDelivery().setDocumentRecipientPostalSvcRegionName(cdo != null ? cdo.getValue() : "");
		}
		if (recip.getRecipientMailDelivery().getDocumentRecipientCountryCode() != null) {

			Collection<CodeLookupBO> cdos = getDocumentDAO().retrieveCountryNameBasedOnCode(
					recip.getRecipientMailDelivery().getDocumentRecipientCountryCode());
			// There is only one record so get first.
			CodeLookupBO cdo = (cdos != null && cdos.size() > 0) ? cdos.iterator().next() : null;
			recip.getRecipientMailDelivery().setDocumentRecipientCountryName(
					cdo != null ? cdo.getCode().toString() : "");
		}
	}

	/**
	 * System option device name is returned from this method for the company.
	 * 
	 * @param deliveryType
	 *            Delivery type code.
	 * @param companyId
	 *            Company Id.
	 * @return String value.
	 */
	private String getSystemOptionDeviceName(String deliveryType, Long companyId) {
		if (printerCode != null && deliveryType.equals(DocumentConstants.DOCUMENT_DELIVERY_TYPE_CODE_MAIL)) {
			return printerCode;
		} else {
			SystemOptionSearchCriteria criteria = new SystemOptionSearchCriteria();
			if (deliveryType.equals(DocumentConstants.DOCUMENT_DELIVERY_TYPE_CODE_MAIL)) {
				criteria.setSystemOptionCode(DELIVER_PRINTER_SYSOPTION);
			} else if (deliveryType.equals(DocumentConstants.DOCUMENT_DELIVERY_TYPE_CODE_EMAIL)) {
				criteria.setSystemOptionCode(DELIVER_EMAIL_SYSOPTION);
			} else if (deliveryType.equals(DocumentConstants.DOCUMENT_DELIVERY_TYPE_CODE_FAX)) {
				criteria.setSystemOptionCode(DELIVER_FAX_SYSOPTION);
			}
			criteria.setCompanyId(companyId);
			criteria.setHierarchyLevel(new Long(3));
			SystemOptionService sysOptServ = MuleServiceFactory.getService(SystemOptionService.class);
			Collection<SystemOptionAssociationDTO> options = sysOptServ.retrieveSystemOptionValues(criteria);

			SystemOptionAssociationDTO option = null;
			if ((options != null) && (options.size() > 0)) {
				option = options.iterator().next();
				return option.getStringValue();
			}
			return "";
		}
	}

	// NOTE: Use this method in 8.0.7 version
	// /**
	// * Retrieve all data retriever based data for document elements.
	// * @param elementsList the list of elements.
	// * @param document Document Outbound BO.
	// * @return map of filled in elements data per request
	// *
	// */
	// public Map<String, DataRetrieverRequest>
	// retrieveDataRetrieverBasedData(Collection<DocumentDataElement>
	// elementsList, DocumentOutboundBO document) {
	// return super.retrieveDataRetrieverBasedData(elementsList, document);
	// }

	/**
	 * Retrieve all data retriever based data for document elements.
	 * 
	 * @param elementsList
	 *            the list of elements.
	 * @param document
	 *            Document Outbound BO.
	 * @return map of filled in elements data per request NOTE: Use this method
	 *         from base class (DocumentServiceHelper) in 8.0.7 version
	 */
	/*@Deprecated
	public Map<String, DataRetrieverRequest> retrieveDataRetrieverBasedData(
			Collection<DocumentDataElement> elementsList, DocumentOutboundBO document) {
		// Create a map to store the data element code and Data retriever
		// request objects. This will be used to retrieve the
		// value from the data retriever.
		Map<String, DataRetrieverRequest> eleRequestMap = new HashMap<String, DataRetrieverRequest>();
		for (DocumentDataElement dde : elementsList) {
			if (dde.getDataRetriever() == null) {
				continue;
			}
			// call getPatameterNames of DRUtils
			DataRetrieverRequest request = MuleServiceFactory.getService(DataRetrieverDocumentService.class)
					.getParameterNames(dde.getDataRetriever());
			// Set derieved values.
			if (request.getParams() != null) {
				for (DataRetrieverParameter param : request.getParams()) {
					if (param.isContext()) {
						param.setValue(document.getAgreementIdDerived());
						continue;
					}
					if (param.isSubcontext()) {
						param.setValue(document.getAgreementSubIdDerived());
						continue;
					}
					if ("agreement_type".equals(param.getName())) {
						param.setValue(document.getAgreementTypeCodeDerived());
						continue;
					}
					for (DocumentAgreementDataBO agreementData : document.getDocumentAgreementData()) {
						if (param.getName().equalsIgnoreCase(agreementData.getAgreementDataCd())) {
							Set<DocumentAgreementDataValueBO> documentAgreementDataValue = agreementData
									.getDocumentAgreementDataValue();
							if (documentAgreementDataValue != null && documentAgreementDataValue.size() == 1) {
								param.setValue(documentAgreementDataValue.iterator().next().getAgreementDataValue());
								break;
							} else if (documentAgreementDataValue != null && documentAgreementDataValue.size() > 1) {
								StringBuffer value = new StringBuffer();
								for (DocumentAgreementDataValueBO dataValue : documentAgreementDataValue) {
									if (value.length() <= 0) {
										value.append(dataValue.getAgreementDataValue());
									} else {
										value.append(", " + dataValue.getAgreementDataValue());
									}
								}
								param.setValue(value.toString());
								break;
							}
						}
					}
				}
				eleRequestMap.put(dde.getDocDataElementCode(), request);
			}
		}
		// Call DR - retrieveAllData()
		try {
			MuleServiceFactory.getService(DataRetrieverDocumentService.class).retrieveAllData(eleRequestMap.values());
		} catch (Exception ex) {
			LOGGER.error("Exception in retrieveAllData", ex);
			throw new DocumentException(DocumentExceptionType.DRNoDataFoundException, ex);
		}
		return eleRequestMap;
	}*/

	/**
	 * @param elementValue
	 *            value in object form for the element that needs to be
	 *            converted to String.
	 * @return the String representation of the value NOTE: Use this method from
	 *         base class (DocumentServiceHelper) in 8.0.7 version
	 */
	//@Deprecated
	/*public String getElementValueAsString(Object elementValue) {
		String stringValue = null;

		if (elementValue instanceof String || elementValue instanceof Boolean) {
			stringValue = (String) elementValue;
		} else if (elementValue instanceof Date) {
			SimpleDateFormat format = new SimpleDateFormat();
			stringValue = format.format(elementValue);
		} else if (elementValue instanceof BigDecimal || elementValue instanceof Long) {
			stringValue = new DecimalFormat().format(elementValue);
		}
		return stringValue;
	}*/

	/**
	 * Helper method a DocumentDTO for a given document ID.
	 * 
	 * @param docId
	 *            document ID
	 * @return DocumentDTO
	 * 
	 */
	public DocumentDTO retrieveDocument(Long docId) {

		return getDocumentService().retrieveDocument(docId);
	}

	/**
	 * Helper method to retrieve the electronic media file for a given document
	 * ID. The electronic media file will be populated during claims migration
	 * indicating the file path for a document on a historical claim.
	 * 
	 * @param docId
	 *            document ID
	 * @return claimNumber
	 * 
	 */
	public String retrieveElectronicMediaFile(Long docId) {

		DocumentElectronicMediaFileDTO documentElectronicMediaFile = null;
		String mediaFileUrl = null;
		try {

			documentElectronicMediaFile = getDocumentService().retrieveElectronicMediaDocument(docId);

			LOGGER.debug("electronic Media file: {} ", documentElectronicMediaFile);

			if (documentElectronicMediaFile != null) {
				mediaFileUrl = documentElectronicMediaFile.getFile();
				LOGGER.debug("Electronic Media File URL: {} ", mediaFileUrl);

			}
		} catch (Exception e) {
			LOGGER.debug("{} caught: {}", e.getClass().getName(), e.getMessage());
		}

		return mediaFileUrl;
	}

	/**
	 * Getter for DocumentService instance.
	 * 
	 * @return the DocumentService implementation
	 */
	public DocumentService getDocumentService() {
		if (documentService == null) {
			documentService = MuleServiceFactory.getService(DocumentService.class);
		}
		return documentService;
	}

}
