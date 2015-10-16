package com.client.iip.integration.batch;

import java.util.Collection;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.client.iip.integration.core.exception.IIPErrorResponse;
import com.client.iip.integration.core.util.ClientQueryHelper;
import com.client.iip.integration.core.util.MuleEndpointAdapter;
import com.fiserv.isd.iip.core.data.LogicalDataSource;
import com.fiserv.isd.iip.core.util.IIPCoreSystemException;
import com.fiserv.isd.iip.core.validation.IIPObjectError;

public class IIPBatchTask extends MuleEndpointAdapter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IIPBatchTask.class);

	private String BATCH_EXPORT_ENDPOINT = "batchExportEndpoint";
	
	private static LogicalDataSource lds;
	
	/**
	 * Getter for lds.
	 * 
	 * @return the lds
	 */
	public LogicalDataSource getLds() {
		return lds;
	}

	/**
	 * Setter for lds.
	 * 
	 * @param ldsParam
	 *            the lds to set
	 */
	public void setLds(LogicalDataSource ldsParam) {
		this.lds = ldsParam;
	}
	
	public void setEndPointName(String _endPointName){
		this.BATCH_EXPORT_ENDPOINT = _endPointName;
	}
	
	public String getEndPointName(){
		return this.BATCH_EXPORT_ENDPOINT;
	}
	
	public  <T> Collection<T>  readItems(String accessId, Class _clazz, Object... params){
		LOGGER.info("LogicalDatSource: " + lds);
		 Collection<T> detailsDTOColl  = ClientQueryHelper.clientQueryList(accessId, 
				 _clazz, lds, params);
		 return detailsDTOColl;
	}
	
	public void writeItems(String _jobName, Object _obj) throws Exception{
		//Send exporatble Data to Mule Endpoint
		HashMap<String, Object> propertyMap = new HashMap<String, Object>();
		propertyMap.put("jobName", _jobName);
		Object payload = sendSyncEvent(getEndPointName(),_obj, propertyMap);
		if(payload!= null){
			if(payload instanceof IIPCoreSystemException){
				//This flow indicates exception was thrown by system
				IIPCoreSystemException coreSystemException = (IIPCoreSystemException)payload;
				LOGGER.error("Exception occurred while writing payload to file endpoint ", coreSystemException);
				throw coreSystemException;
			}else if(payload instanceof Collection){
				//This flow indicates response XML validation error on the stream side
				for(Object obj:(Collection<?>)payload){
					if (obj instanceof IIPErrorResponse){
						Collection<IIPObjectError> iipObjectErrors = ((IIPErrorResponse) obj).getFormattedErrors();
						if(iipObjectErrors != null && !iipObjectErrors.isEmpty()){
							for (IIPObjectError ioe : iipObjectErrors) {
								String errorMessage = ioe.getFormattedMessage();
								LOGGER.error("Exception occurred while writing payload to file endpoint ", errorMessage);
								IIPCoreSystemException ex = new IIPCoreSystemException(errorMessage);
								throw ex;
							}
						}					
					}
				}
			}
		}						
	}
}
