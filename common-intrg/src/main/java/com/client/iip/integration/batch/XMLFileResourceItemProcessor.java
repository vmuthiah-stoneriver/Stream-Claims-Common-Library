package com.client.iip.integration.batch;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.core.io.Resource;

import com.client.iip.integration.core.converter.IIPXStreamDateTimestampConverter;
import com.client.iip.integration.core.transformer.IIPXSLTransformer;
import com.client.iip.integration.core.util.IIPXStream;
import com.client.iip.integration.core.util.XSDValidator;
import com.thoughtworks.xstream.XStream;

public class XMLFileResourceItemProcessor extends IIPXSLTransformer implements ItemProcessor<Resource, Map<File, Object>> {
	
	private static final Log logger = LogFactory.getLog(XMLFileResourceItemProcessor.class);
	
	private String xsdFileName = null;
	
	private List<String> aliasFileList;
	
	@javax.annotation.Resource(name = "stepExecutionListener")  
	private StepExecutionListenerCtxInjecter stepExecutionListener;
	
	public XMLFileResourceItemProcessor() throws Exception{
		if(getXslFile() != null){
			super.initialise();
		}
	}
	
	public void setSchemaFileName(String _xsdFileName){
		xsdFileName = _xsdFileName;
	}

	/**
	 * @param aliasFileList
	 *            the aliasFileList to set
	 */
	public void setAliasFileList(List<String> aliasFileList) {
		this.aliasFileList = aliasFileList;
	}	
	
	public Map<File, Object> process(Resource _resource) throws Exception{
		Map<File, Object> resourceMap = new HashMap<File, Object>();
		
		try{
			String responseXML = null;
			String strXML = FileUtils.readFileToString(_resource.getFile());
			//2.Validate against XSD
			if(strXML != null && !strXML.isEmpty()){
				//Transform the xml if xsl is provided.
				if (getXslFile() != null){
					MuleMessage message = new DefaultMuleMessage(strXML, new HashMap(), muleContext);
					MuleMessage response = (MuleMessage)transformMessage(message, "UTF-8");
					responseXML = (String)response.getPayload();
				}else{
					responseXML = strXML;
				}
				
				if(responseXML != null){
					XSDValidator xsdval = new XSDValidator();
					xsdval.validateSchema(responseXML, xsdFileName);
					//3.Convert XML into Object
					//Load the resource file along with the read data
					resourceMap.put(_resource.getFile(), getXStreamInstance().convert2Object(responseXML));
				}
			}			
		}catch(Exception ex){
			logger.error("Failure when processing resource", ex);
			BatchUtils.writeIntoBatchLog(stepExecutionListener.getStepExecution(), "clm", "Resource processing Failure : " 
					+ _resource.getFile(), ex);
			resourceMap = null;
			//throw new Exception("Failure when processing resource : " + _resource.getFile().getName(), ex);
		}
		
		return resourceMap;
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
