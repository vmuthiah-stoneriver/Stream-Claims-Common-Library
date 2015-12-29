package com.client.iip.integration.batch;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;
import org.springframework.oxm.XmlMappingException;

import com.client.iip.integration.core.converter.CustomReflectionConverter;
import com.client.iip.integration.core.converter.IIPXStreamDateConverter;
import com.client.iip.integration.core.transformer.IIPXSLTransformer;
import com.client.iip.integration.core.util.IIPXStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;


public class XMLFileResourceItemWriter extends IIPXSLTransformer implements ItemWriter<Object>  {
	
	private static final Log logger = LogFactory.getLog(XMLFileResourceItemWriter.class);
	
	private String exportFolderPath = null;
	
	private List<String> aliasFileList;
	
	private String jobName;
	
	private String fileName;
	
	private String fileExtension = "xml";
	
	@javax.annotation.Resource(name = "stepExecutionListener")  
	private StepExecutionListenerCtxInjecter stepExecutionListener;	
	

	public XMLFileResourceItemWriter() throws Exception{
		if(getXslFile() != null){
			super.initialise();
		}
	}

	/**
	 * @return the aliasFileList
	 */
	public List<String> getAliasFileList() {
		return aliasFileList;
	}

	/**
	 * @param aliasFileList the aliasFileList to set
	 */
	public void setAliasFileList(List<String> aliasFileList) {
		this.aliasFileList = aliasFileList;
	}

	/**
	 * @return the fileExtension
	 */
	public String getFileExtension() {
		return fileExtension;
	}

	/**
	 * @param fileExtension the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	/**
	 * @return the exportFolderPath
	 */
	public String getExportFolderPath() {
		return exportFolderPath;
	}
	
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		//Generate fileName if not set.
		if(this.fileName == null){
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH-mm-ss.SSS");
			String formattedDate = dateFormat.format(new Date());			
			return jobName + "_" + formattedDate  + "." + fileExtension;
		}else{
			return this.fileName;
		}
	}

	/**
	 * @param exportFolderPath the exportFolderPath to set
	 */
	public void setExportFolderPath(String exportFolderPath) {
		this.exportFolderPath = exportFolderPath;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.springframework.batch.core.StepExecutionListener#beforeStep(org.springframework.batch.core.StepExecution)
	 * Sort the Files before Job Step
	 */
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
	}


	/**
	 * Write the value objects and flush them to the file.
	 * 
	 * @param items the value object
	 * @throws IOException
	 * @throws XmlMappingException
	 */
	public void write(List<? extends Object> items) throws Exception {
		logger.debug("Inside writer");
		try{
			//Loop through objects, Convert to xml and write to file
			for (Object object : items) {			
				String responseXML = null;
				//Convert object to xml string
				String strXML = getXStreamInstance().convert2XML(object);
				//logger.debug("XML : " + strXML);

				//Transform xml if xsl is provided
				if (getXslFile() != null){
					MuleMessage message = new DefaultMuleMessage(strXML, new HashMap(), muleContext);
					MuleMessage response = (MuleMessage)transformMessage(message, "UTF-8");
					responseXML = (String)response.getPayload();
				}else{
					responseXML = strXML;
				}
				
				//Write to File			
				if(responseXML != null){
					FileUtils.writeStringToFile(new File(getExportFolderPath() + File.separator + getFileName()), responseXML);
				}else{
					logger.error("Error in Transformation responseXML : " + responseXML);
				}
			}
		}catch(Exception ex){
			logger.error("Failure when writing resource", ex);
			BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm" , "Resource write Failure : ", ex);
			//throw new Exception("XMLFileResourceItemWriter Failed : ", ex);			
		}

	}

	public IIPXStream getXStreamInstance() throws Exception{
		IIPXStream xstream = new IIPXStream(aliasFileList);
		xstream.registerConverter(new CustomReflectionConverter(xstream.getMapper(),new PureJavaReflectionProvider()));
		xstream.registerConverter(new IIPXStreamDateConverter());
		xstream.addDefaultImplementation(java.sql.Date.class, java.util.Date.class);
		xstream.addDefaultImplementation(java.sql.Timestamp.class, java.util.Date.class);
		xstream.addDefaultImplementation(java.sql.Time.class, java.util.Date.class);
		xstream.addDefaultImplementation(java.util.ArrayList.class, java.util.List.class);
		xstream.addDefaultImplementation(java.util.HashMap.class, java.util.Map.class);			
		return xstream;
	}



}
