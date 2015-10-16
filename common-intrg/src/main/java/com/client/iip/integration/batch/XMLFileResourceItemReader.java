package com.client.iip.integration.batch;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.client.iip.integration.core.converter.IIPXStreamDateTimestampConverter;
import com.client.iip.integration.core.util.IIPXStream;
import com.client.iip.integration.core.util.XSDValidator;
import com.thoughtworks.xstream.XStream;

/**
 * Item reader for reading XML input.
 * 
 * It extracts fragments from the input XML document which correspond to records for processing. The fragments are
 * then validated aginst XSD and converted to Stream DO's based on the mapped DTO properties
 * 
 * @author G. Radhakrishnan
 */
public class XMLFileResourceItemReader<T> extends AbstractItemCountingItemStreamItemReader<T> implements
		ResourceAwareItemReaderItemStream<T>, InitializingBean {

	private static final Log logger = LogFactory.getLog(XMLFileResourceItemReader.class);

	private List<String> aliasFileList;

	private Resource resource;

	private InputStream inputStream;
	
	private String xsdFileName;

	private boolean noInput;

	private boolean strict = true;


	public XMLFileResourceItemReader() {
		setName(ClassUtils.getShortName(XMLFileResourceItemReader.class));
	}
	
	@javax.annotation.Resource(name = "stepExecutionListener")  
	private StepExecutionListenerCtxInjecter stepExecutionListener;

	/**
	 * In strict mode the reader will throw an exception on
	 * {@link #open(org.springframework.batch.item.ExecutionContext)} if the input resource does not exist.
	 * @param strict false by default
	 */
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}
	
	public Resource getResource() {
		return this.resource;
	}	
	
	public void setSchemaFileName(String _xsdFileName){
		xsdFileName = _xsdFileName;
	}
	
	public String getSchemaFileName(){
		return xsdFileName;
	}	
	
	public InputStream getInputStream(){
		return inputStream;
	}
	
	/**
	 * @return the aliasFileList
	 */
	public List<String> getAliasFileList() {
		return aliasFileList;
	}

	/**
	 * @param aliasFileList
	 *            the aliasFileList to set
	 */
	public void setAliasFileList(List<String> aliasFileList) {
		this.aliasFileList = aliasFileList;
	}
	
	/**
	 * @return the noInput
	 */
	public boolean isNoInput() {
		return noInput;
	}

	/**
	 * @param noInput the noInput to set
	 */
	public void setNoInput(boolean noInput) {
		this.noInput = noInput;
	}

	public void afterPropertiesSet() throws Exception {
		logger.info("After Properties Set");
	}	

	protected void doClose() throws Exception {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		}
		finally {
			inputStream = null;
		}

	}

	protected void doOpen() throws Exception {
		Assert.notNull(resource, "The Resource must not be null.");

		noInput = true;
		if (!resource.exists()) {
			if (strict) {
				throw new IllegalStateException("Input resource must exist (reader is in 'strict' mode)");
			}
			logger.warn("Input resource does not exist " + resource.getDescription());
			return;
		}
		if (!resource.isReadable()) {
			if (strict) {
				throw new IllegalStateException("Input resource must be readable (reader is in 'strict' mode)");
			}
			logger.warn("Input resource is not readable " + resource.getDescription());
			return;
		}

		inputStream = resource.getInputStream();
		noInput = false;

	}

	/**
	 * Read Resource
	 */
	protected synchronized T doRead() throws Exception {

		if (noInput) {
			return null;
		}

		T item = null;

		try {
			//1.Read InputStream to String
			String strXML = null;
			strXML = IOUtils.toString(inputStream);
			//2.Validate against XSD
			if(strXML != null && !strXML.isEmpty() && xsdFileName != null){
				XSDValidator xsdval = new XSDValidator();
				xsdval.validateSchema(strXML, xsdFileName);
				//3.Convert XML into Object
				//Load the resource file along with the read data
				Object obj = getXStreamInstance().convert2Object(strXML);
				item =  (T) obj;
			}else if(strXML != null){
				item =  (T) getResource();
			}
		}
		catch (Exception ex) {
			// Prevent caller from retrying indefinitely since this is fatal
			logger.error("Exception occured while converting XML to Object", ex);
			BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm", "Read Failure : " 
					+ this.resource.getFile(), ex);
			noInput = true;
			throw ex;
		}

		return item;
	}

	public IIPXStream getXStreamInstance() throws Exception{
		IIPXStream xstream = new IIPXStream(aliasFileList);
		xstream.registerConverter(new IIPXStreamDateTimestampConverter(),
				XStream.PRIORITY_VERY_HIGH);
		xstream.addDefaultImplementation(java.util.ArrayList.class, java.util.Collection.class);
		xstream.addDefaultImplementation(java.util.HashMap.class, java.util.Map.class);		
		return xstream;
	}


}

