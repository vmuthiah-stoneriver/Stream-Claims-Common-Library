/**
 * 
 */
package com.client.iip.integration.documents;

import java.util.ArrayList;
import java.util.List;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.transformer.IIPObject2XMLTransformer;

/**
 * Document message transformer to be used for building XML message.
 * 
 * @author sudharsan.sriram
 *
 */
public class DocumentTransformer extends AbstractMessageTransformer {

	private Logger logger = LoggerFactory.getLogger(DocumentTransformer.class);
	
	/**
	 * Transform the message.
	 */
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		
		Object responsePayload = null;
		
		Object payload = message.getPayload();
		
		if(payload != null){
		//payload is in the required String format
		if(payload instanceof String){
			logger.info("payload is in the required String format");
			return payload;
		}
		
		//Transform Object to XML
		IIPObject2XMLTransformer transformer = new IIPObject2XMLTransformer();
		
		//Set empty alias file list
		List<String> aliasFileList= new ArrayList<String>();
		transformer.setAliasFileList(aliasFileList);
		
		responsePayload = transformer.transformMessage(message, outputEncoding);
		}
		
		return responsePayload;
	}

}
