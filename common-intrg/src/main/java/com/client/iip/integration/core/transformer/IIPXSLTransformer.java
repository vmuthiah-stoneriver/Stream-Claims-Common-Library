package com.client.iip.integration.core.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transformer.TransformerException;
import org.mule.api.transport.PropertyScope;
import org.mule.module.xml.transformer.XsltTransformer;
import org.mule.module.xml.util.LocalURIResolver;
import org.mule.transformer.types.DataTypeFactory;
import org.mule.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.fiserv.isd.iip.core.messageresolver.MessageConstants;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

public class IIPXSLTransformer extends XsltTransformer {
	
	private final Logger logger = LoggerFactory.getLogger(IIPXSLTransformer.class);
	public static final String INTERFACE_PROPERTY = "interface";
	private String xslFile = null;
	
	public IIPXSLTransformer(){
		super();
		registerSourceType(DataTypeFactory.create(Collection.class));
		setReturnDataType(DataTypeFactory.STRING);
	}
	
    @Override
    public void initialise() throws InitialisationException
    {
        logger.info("Initialising transformer: " + xslFile);
        try
        {
            // Only load the file once at initialize time
            if (xslFile != null)
            {
                super.setXslt(IOUtils.getResourceAsString(xslFile, getClass()));
                super.setUriResolver(new LocalURIResolver(xslFile));
            }
            transformerPool.addObject();
        }
        catch (Throwable te)
        {
            throw new InitialisationException(te, this);
        }
    }	

	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		// TODO Auto-generated method stub
		logger.info("Inside IIPXSLTransformer");

		try{
			if(message.getPayload() != null && message.getPayload() instanceof String){
				String payload = (String)message.getPayload();
				// Check whether payload has system exception
				if (payload.trim().length() > 0 && !payload.contains("iipCoreSystemException") 
						&& !payload.contains("ErrorResponse")) {
					message.setPayload(super.transformMessage(message, outputEncoding));
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			logger.error("XML/XSL Transformation Failed: ", ex);
			message.setProperty(INTERFACE_PROPERTY, "INBNDERR" , PropertyScope.OUTBOUND);
			message.setProperty("http.status", 400 , PropertyScope.OUTBOUND);		
			Collection<IIPErrorResponse> errResponse = new ArrayList<IIPErrorResponse>();
			IIPErrorResponse thisError = new IIPErrorResponse();
			Collection<IIPObjectError> ioes = new ArrayList<IIPObjectError>();
			String[] codes = new String[] {"XSLTransformationfailure"};
			IIPObjectError ioe = new IIPObjectError("IIPXSLTransformer", "transformMessage",
					null, codes, null, MessageConstants.SEVERITY_ERROR);
			ioe.setFormattedMessage(ex.toString());
			ioes.add(ioe);
			thisError.setFormattedErrors(ioes);
			errResponse.add(thisError);
			message.setPayload(errResponse);
			message.setExceptionPayload(null);
		}
		setReturnDataType(DataTypeFactory.create(MuleMessage.class));
        return message;
	}
	
	public void setXslFile(String xslFileParam){
		try {
			this.xslFile = xslFileParam;
			super.setXslt(IOUtils.getResourceAsString(xslFile, getClass()));
			super.setUriResolver(new LocalURIResolver(xslFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getXslFile() {
		return this.xslFile;
	}

}
