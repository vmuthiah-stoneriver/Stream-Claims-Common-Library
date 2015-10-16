package com.client.iip.integration.documents;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.stoneriver.iip.document.DocumentDAO;
import com.stoneriver.iip.document.DocumentDataElement;
import com.stoneriver.iip.document.DocumentDataElementCodeBO;
import com.stoneriver.iip.document.DocumentDataElementTableCodeBO;
import com.stoneriver.iip.document.DocumentDataElementTableColumnCodeBO;
import com.stoneriver.iip.document.api.DocumentRenderException;


/**
 * Batch Job for creating XML Schema for Out bound Documents. 
 * @author vmuthiah
 *
 */
@SuppressWarnings("rawtypes")
public class XMLSchemaWriter implements ItemWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLSchemaWriter.class);

	private DocumentDAO documentDAO;
	private ClientDocumentDAO clientDocumentDAO;
	//private Configuration config;
	//private static final String FREEMARKER_CONFIG_ID = "xmlschema.config.freemarkerConfiguration";
	private String outputXMLSchemaFile;
	private XMLSchemaHelper xmlSchemHelper;

	/**
	 * @param configuration the config to set
	 */
	/*@Inject(PojoRef=FREEMARKER_CONFIG_ID)
	public void setConfig(Configuration configuration) {
		this.config = configuration;
	}*/

	/**
	 * XML Schema generator batch.
	 * 
	 * @param items Number of Document Data Elements configured  
	 * @throws Exception to be caught
	 */
	public void write(List documentDataElementCode) throws Exception {
		LOGGER.info("Entering write() method to generate XML Schema.");
		
		if(documentDataElementCode == null || documentDataElementCode.isEmpty()) {
			LOGGER.info("No Document Data Elements configured for generating XML Schema.");
			return;
		}
		
		Collection<DocumentDataElement> documentDataElements = clientDocumentDAO.retrieveDocumentDataElements();
		LOGGER.debug("documentDataElements {}", documentDataElements);
		
		
		//Variable to store Columns Grouped together for each data element
		Map<String, Set<DocumentDataElementTableColumnCodeBO>> groupedColumnsByDataElementCode = new HashMap<String, Set<DocumentDataElementTableColumnCodeBO>>();
		//Map contains the Table Data ElementColumns by Data Element Code
		Map<String, List<DocumentDataElementTableColumnCodeBO>> tableColumnDataElements = new HashMap<String, List<DocumentDataElementTableColumnCodeBO>>();

		//Map containing Table Column XML tags with Key as Data Element Code and value as a List of Table Column XML tags
		Map<String, List<String>> tableColumnDuplicates = new HashMap<String, List<String>>();

		Collection<DocumentDataElementCodeBO> documentDataElementCodes = documentDAO.fetchAllDocumentDataElements();
		LOGGER.debug("documentDataElementCodes {}", documentDataElementCodes);
		
		for(DocumentDataElementCodeBO documentElementCodeBO : documentDataElementCodes) {
			if(documentElementCodeBO instanceof DocumentDataElementTableCodeBO){
				xmlSchemHelper.processGroupedColumns((DocumentDataElementTableCodeBO) documentElementCodeBO,
						groupedColumnsByDataElementCode, tableColumnDataElements, tableColumnDuplicates);
			}
		}

		if(documentDataElements != null 
				&& !documentDataElements.isEmpty()) {
			try {
				xmlSchemHelper.createXMLSchema(documentDataElements, getOutputXMLSchemaFile(), groupedColumnsByDataElementCode,
						tableColumnDataElements);
			} catch (DocumentRenderException e) {
				LOGGER.error("Unable to render XSD : ", e);
				throw e;
			}
		}
	}
	
	/**
	 * @return the clientDocumentDAO
	 */
	public DocumentDAO getDocumentDAO() {
		return documentDAO;
	}


	/**
	 * @param value DocumentDAO to set
	 */
	public void setDocumentDAO(DocumentDAO value) {
		this.documentDAO = value;
	}

	/**
	 * @return Output Schema File
	 */
	public String getOutputXMLSchemaFile() {
		return outputXMLSchemaFile;
	}

	/**
	 * @param file Output Schema File to set
	 */
	public void setOutputXMLSchemaFile(String file) {
		this.outputXMLSchemaFile = file;
	}

	/**
	 * @param pojo the ClaimImportHelper to set
	 */
	@Inject(PojoRef="documents.client.xmlSchemaHelper")
	public void setXmlSchemHelper(XMLSchemaHelper pojo) {
		this.xmlSchemHelper = pojo;
	}

	/**
	 * @return the clientDocumentDAO
	 */
	public ClientDocumentDAO getClientDocumentDAO() {
		return clientDocumentDAO;
	}

	/**
	 * @param dao the clientDocumentDAO to set
	 */
	public void setClientDocumentDAO(ClientDocumentDAO dao) {
		this.clientDocumentDAO = dao;
	}
}
