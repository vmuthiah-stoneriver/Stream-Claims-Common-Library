package com.client.iip.integration.documents;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mule.api.MuleMessage;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.xml.sax.InputSource;

import com.fiserv.isd.iip.core.meta.annotation.Inject;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.stoneriver.iip.document.api.DocumentRenderException;
import com.stoneriver.iip.document.render.DocumentRenderRequest;

import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class OutBoundDocumentXMLTransformer extends AbstractMessageTransformer {
	private List<String> aliasFileList;
	private final Logger logger = LoggerFactory.getLogger(OutBoundDocumentXMLTransformer.class);
	
	//Template Configuration instance
	private Configuration templateConfiguration;
	private static final String FREEMARKER_CONFIG_ID = "xmlschema.config.freemarkerConfiguration";
	private static final String TEMPLATE_NAME = "OutboundXMLTransformer.FTL";
	
	/**
	 * @param configuration Template Configuration to set
	 */
	@Inject(PojoRef=FREEMARKER_CONFIG_ID)
	public void setConfig(Configuration configuration) {
		this.templateConfiguration = configuration;
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

	/* (non-Javadoc)
	 * @see org.mule.transformer.AbstractMessageTransformer#transformMessage(org.mule.api.MuleMessage, java.lang.String)
	 */
	public Object transformMessage(MuleMessage message, String outputEncoding)
			throws TransformerException {
		Object payload = message.getPayload();
		
		String transformedXML = null;
		if(payload instanceof DocumentRenderRequest) {
			String outboundXML = ((DocumentRenderRequest)payload).getXml();
			transformedXML = transformXML(outboundXML);
		}
		if(transformedXML == null) {
			return message;			
		} else {
			return transformedXML;
		}
	}
	
	/**
	 * @param outboundXMLDocument
	 * @return transformed XML String
	 */
	private String transformXML(String outboundXMLDocument) {
		Template tmplt = null;
		String transformedXML = null;
		try {
			templateConfiguration.setClassForTemplateLoading(this.getClass(), "/");

			tmplt = templateConfiguration.getTemplate("iip-integration-config/" + TEMPLATE_NAME);
		
			Map<String, NodeModel> root = new HashMap<String, NodeModel>();
			
			root.put(
	        "doc",
	        freemarker.ext.dom.NodeModel.parse( new InputSource(new StringReader(outboundXMLDocument))));
			
			transformedXML = FreeMarkerTemplateUtils.processTemplateIntoString(tmplt, root);
			logger.debug("**********************");
			logger.debug("transformedXML: {} ", transformedXML);
			logger.debug("**********************");
		} catch (IOException e) {
			throw new IIPCoreSystemException("No template name matches  '" 
						+ TEMPLATE_NAME + "'." , e);
		} catch (TemplateException e) {
			throw new DocumentRenderException("Unable to process transform XML using template " + tmplt , e);
		} catch (Exception e) {
			logger.error("Unable to process transform XML using template - {}", tmplt, e.getMessage());
			throw new IIPCoreSystemException("Unable to process transform XML using template " + tmplt , e);
		}
		return transformedXML;
	}

}
