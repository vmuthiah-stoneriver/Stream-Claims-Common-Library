/**
 * 
 */
package com.client.iip.integration.documents;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.meta.annotation.Pojo;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.document.DocumentConstants;
import com.stoneriver.iip.document.DocumentDataElement;
import com.stoneriver.iip.document.DocumentDataElementTableCodeBO;
import com.stoneriver.iip.document.DocumentDataElementTableColumnCodeBO;
import com.stoneriver.iip.document.TableColumnComparator;
import com.stoneriver.iip.document.api.DocumentRenderException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Helper class for generating XML Schema
 * @author vmuthiah
 *
 */
@Pojo(id = "documents.client.xmlSchemaHelper")
public class XMLSchemaHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(XMLSchemaHelper.class);

	//Template Configuration instance
	private Configuration templateConfiguration;
	private static final String FREEMARKER_CONFIG_ID = "xmlschema.config.freemarkerConfiguration";
	private static final String TEMPLATE_NAME = "OutboundXMLSchema.FTL";
	
	/**
	 * @param configuration Template Configuration to set
	 */
	@Inject(PojoRef=FREEMARKER_CONFIG_ID)
	public void setConfig(Configuration configuration) {
		this.templateConfiguration = configuration;
	}

	/**
	 * Create XML Schema with the given Document Data Elements and Template. The schema will be created in the directory specified 
	 * in Schema Name parameter 
	 * 
	 * @param allDataElements Document Data Elements to render.
	 * @param xmlSchemaName the name of the file and directory that will be created
	 * @param groupedColumnsByDataElementCode Variable to store Columns Grouped together for each data element
	 * @param tableColumnDataElements Map contains the Table Data ElementColumns by Data Element Code
	 * @return String XML Schema.
	 */
	public String createXMLSchema(Collection<DocumentDataElement> allDataElements, String xmlSchemaName,
			Map<String, Set<DocumentDataElementTableColumnCodeBO>> groupedColumnsByDataElementCode, 
			Map<String, List<DocumentDataElementTableColumnCodeBO>> tableColumnDataElements) {
		Template t = null;
		String xsdString = null;
		try {
			//Map contains the Single Data Elements by Group
			Map<String, Map<String, List<DocumentDataElement>>> singleDataElements = new HashMap<String, Map<String, List<DocumentDataElement>>>();
			//Map contains the Table Data Elements by Group
			Map<String, Map<String, List<DocumentDataElement>>> tableDataElements = new HashMap<String, Map<String, List<DocumentDataElement>>>();
			
			Map<String, List<String>> singleDocumentDataElementByGroup = new HashMap<String, List<String>>();
			Map<String, List<String>> tableDocumentDataElementByGroup = new HashMap<String, List<String>>();
			
			if(allDataElements != null && !allDataElements.isEmpty()) {
				for(DocumentDataElement dataElement : allDataElements) {
					if(dataElement.getDataElementTypeCode().equals(DocumentConstants.DOCUMENT_DATA_ELEMENT_TYPE_CD_SINGLE)) {
						
						//Check if the agreement type is already added. If added check if the Group is added
						if(singleDataElements.containsKey(dataElement.getAgreementTypeXMLId())) {
							Map<String, List<DocumentDataElement>> mapGroupDataElements = 
								singleDataElements.get(dataElement.getAgreementTypeXMLId());
							//Check if the Group is already added
							if(mapGroupDataElements.containsKey(dataElement.getGroupCodeXMLId())) {
								//If the group is already added to the map check if the document element is already added for the group
								List<String> singleDataElement = singleDocumentDataElementByGroup.get(dataElement.getAgreementTypeXMLId() + dataElement.getGroupCodeXMLId());
								//If the document element is not added for the group then add it to the List
								if(!singleDataElement.contains(dataElement.getDocDataXMLId())) {
									singleDataElement.add(dataElement.getDocDataXMLId());
									
									mapGroupDataElements.get(dataElement.getGroupCodeXMLId()).add(dataElement);
								}
							} else {
								//If the group is not added to the map then add the document element
								List<DocumentDataElement> dataElements = new ArrayList<DocumentDataElement>();
								dataElements.add(dataElement);
								mapGroupDataElements.put(dataElement.getGroupCodeXMLId(), dataElements);

								//Add it Map to check for Duplicates.
								List<String> singleDataElement = new ArrayList<String>();
								singleDataElement.add(dataElement.getDocDataXMLId());
								singleDocumentDataElementByGroup.put(dataElement.getAgreementTypeXMLId() + dataElement.getGroupCodeXMLId(), singleDataElement);
							}
						} else {
							//if the agreement type is not added not to the map then add the document element.
							Map<String, List<DocumentDataElement>> mapGroupDataElements =
								new HashMap<String, List<DocumentDataElement>>(); 
							
							List<DocumentDataElement> dataElements = new ArrayList<DocumentDataElement>();
							dataElements.add(dataElement);
							
							mapGroupDataElements.put(dataElement.getGroupCodeXMLId(), dataElements);
							
							singleDataElements.put(dataElement.getAgreementTypeXMLId(), mapGroupDataElements);

							//Add it Map to check for Duplicates.
							List<String> singleDataElement = new ArrayList<String>();
							singleDataElement.add(dataElement.getDocDataXMLId());
							singleDocumentDataElementByGroup.put(dataElement.getAgreementTypeXMLId() + dataElement.getGroupCodeXMLId(), singleDataElement);
						}
					} else {
						//Check if the agreement type is already added. If added check if the Group is added
						if(tableDataElements.containsKey(dataElement.getAgreementTypeXMLId())) {
							Map<String, List<DocumentDataElement>> mapGroupDataElements = 
								tableDataElements.get(dataElement.getAgreementTypeXMLId());
							//Check if the Group is already added
							if(mapGroupDataElements.containsKey(dataElement.getGroupCodeXMLId())) {
								//If the group is already added to the map check if the document element is already added for the group
								List<String> tableDataElement = tableDocumentDataElementByGroup.get(dataElement.getAgreementTypeXMLId() + dataElement.getGroupCodeXMLId());
								//If the document element is not added for the group then add it to the List
								if(!tableDataElement.contains(dataElement.getDocDataXMLId())) {
									tableDataElement.add(dataElement.getDocDataXMLId());
									mapGroupDataElements.get(dataElement.getGroupCodeXMLId()).add(dataElement);
								}
							} else {
								//If the group is not added to the map then add the document element
								List<DocumentDataElement> dataElements = new ArrayList<DocumentDataElement>();
								dataElements.add(dataElement);
								mapGroupDataElements.put(dataElement.getGroupCodeXMLId(), dataElements);

								//Add it Map to check for Duplicates.
								List<String> tableDataElement = new ArrayList<String>();
								tableDataElement.add(dataElement.getDocDataXMLId());
								tableDocumentDataElementByGroup.put(dataElement.getAgreementTypeXMLId() + dataElement.getGroupCodeXMLId(), tableDataElement);
							}
						} else {
							//if the agreement type is not added not to the map then add the document element.
							Map<String, List<DocumentDataElement>> mapGroupDataElements =
								new HashMap<String, List<DocumentDataElement>>(); 
							
							List<DocumentDataElement> dataElements = new ArrayList<DocumentDataElement>();
							dataElements.add(dataElement);
							
							mapGroupDataElements.put(dataElement.getGroupCodeXMLId(), dataElements);
							
							tableDataElements.put(dataElement.getAgreementTypeXMLId(), mapGroupDataElements);

							//Add it Map to check for Duplicates.
							List<String> tableDataElement = new ArrayList<String>();
							tableDataElement.add(dataElement.getDocDataXMLId());
							tableDocumentDataElementByGroup.put(dataElement.getAgreementTypeXMLId() + dataElement.getGroupCodeXMLId(), tableDataElement);
						}
					}					
				}	
			}
			
			templateConfiguration.setClassForTemplateLoading(this.getClass(), "/");

			t = templateConfiguration.getTemplate("iip-integration-config/" + TEMPLATE_NAME);

			HashMap<String, Object> model = new HashMap<String, Object>();
			model.put("singleDataElements", singleDataElements);
			model.put("tableDataElements", tableDataElements);
			model.put("groupedColumns", groupedColumnsByDataElementCode);
			model.put("tableColumnDataElements", tableColumnDataElements);
			
			xsdString = FreeMarkerTemplateUtils.processTemplateIntoString(t, model);
			
			FileWriter fw = new FileWriter(xmlSchemaName);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(xsdString);
			bw.close();
			fw.close();
			bw = null;
			fw = null;

			LOGGER.debug("----Free Market template Xml Result in OutboundRenderer----- {} ", xsdString);
		} catch (IOException e) {
			throw new IIPCoreSystemException("No template name matches  '" 
						+ TEMPLATE_NAME + "'." , e);
		} catch (TemplateException e) {
			throw new DocumentRenderException("Unable to process template into XSD String. " + t , e);
		}
		return xsdString;
	}

	/**
	 * @param tableCodeBO data elements to check for grouped columns
	 * @param groupedColumnsByDataElementCode Variable to store Columns Grouped together for each data element
	 * @param tableColumnDataElements Map contains the Table Data ElementColumns by Data Element Code
	 * @param tableColumnDuplicates Map containing Table Column XML tags with Key as Data Element Code and value as a List of Table Column XML tags
	 */
	public void processGroupedColumns(DocumentDataElementTableCodeBO tableCodeBO, 
			Map<String, Set<DocumentDataElementTableColumnCodeBO>> groupedColumnsByDataElementCode, 
			Map<String, List<DocumentDataElementTableColumnCodeBO>> tableColumnDataElements, 
			Map<String, List<String>> tableColumnDuplicates) {
		TreeSet<DocumentDataElementTableColumnCodeBO> groupedColumns = new TreeSet<DocumentDataElementTableColumnCodeBO>(new TableColumnComparator());
		Collection<DocumentDataElementTableColumnCodeBO> columns = tableCodeBO.getColumns();
		//List containing the Group Table Column XML tags
		List<String> groupedColumnDuplicates = new ArrayList<String>();
		
		for(DocumentDataElementTableColumnCodeBO col : columns){
			if(col.getGroupByIndicator() && col.getGroupByPosition() != null){
				if(!groupedColumnDuplicates.contains(col.getXmlId())) {
					groupedColumns.add(col);
					groupedColumnDuplicates.add(col.getXmlId());
				}
			} else {
				if(tableColumnDataElements.containsKey(tableCodeBO.getDocumentDataElementCode())) {
					
					List<String> existingTableColumnTags = tableColumnDuplicates.get(tableCodeBO.getDocumentDataElementCode());
					if(!existingTableColumnTags.contains(col.getXmlId())) {
						List<DocumentDataElementTableColumnCodeBO> tableColumns = 
							tableColumnDataElements.get(tableCodeBO.getDocumentDataElementCode());
						tableColumns.add(col);
						
						existingTableColumnTags.add(col.getXmlId());
					}
				} else {
					List<DocumentDataElementTableColumnCodeBO> tableColumns =
						new ArrayList<DocumentDataElementTableColumnCodeBO>();
					tableColumns.add(col);
					tableColumnDataElements.put(tableCodeBO.getDocumentDataElementCode(), tableColumns);
					
					List<String> existingTableColumnTags = new ArrayList<String>();
					existingTableColumnTags.add(col.getXmlId());
					tableColumnDuplicates.put(tableCodeBO.getDocumentDataElementCode(), existingTableColumnTags);
				}
			}
		}
		if(!groupedColumns.isEmpty()) {
			groupedColumnsByDataElementCode.put(tableCodeBO.getDocumentDataElementCode(), groupedColumns);
		}
	}	
}
